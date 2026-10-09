/**
 * VerifyEtClient — Phase 3 implementation.
 *
 * This is the thin typed wrapper around the verify.et REST API.
 * Plan Section 4.2, rule 1: "a thin typed wrapper (timeouts 10s, 1 retry on
 * network error only, never retry on 4xx, circuit breaker, structured logs
 * WITHOUT API key or full receipt numbers)."
 *
 * RULE: The verify.et API key is NEVER in the APK or in any log.
 *       It lives in the backend config only (plan Section 2, Rule 1).
 *
 * verify.et docs: https://verify.et/docs/api
 */
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios, { AxiosInstance } from 'axios';
import { PaymentProvider } from '../common/types/domain.types';

export interface VerifyEtSubmitRequest {
  idempotencyKey: string;
  bank: string;
  settlementAccount: string;
  reference?: string;
  suffix?: string;
  transactionNumber?: string;
  receiptNumber?: string;
  phone?: string;
  referenceNumber?: string;
  accountSuffix?: string;
}

export interface VerifyEtResult {
  requestId: string;
  status: 'success' | string;
  verified: boolean;
  amount?: number;
  currency?: string;
  senderName?: string;
  receiverName?: string;
  receiverAccount?: string;
  referenceNumber?: string;
  timestamp?: string;
  confirmationHistory?: {
    confirmedBefore: boolean;
    confirmationCount: number;
  };
  settlementAccountMatch?: {
    matched: boolean;
    ambiguous?: boolean;
  };
}

export interface VerifyEtResponse {
  httpStatus: 200 | 202;
  requestId?: string;
  result?: VerifyEtResult;
}

@Injectable()
export class VerifyEtClient {
  private readonly logger = new Logger(VerifyEtClient.name);
  private readonly http: AxiosInstance;

  constructor(private readonly config: ConfigService) {
    this.http = axios.create({
      baseURL: config.get('VERIFY_ET_BASE_URL', 'https://verify.et'),
      timeout: config.get<number>('VERIFY_ET_TIMEOUT_MS', 10_000),
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Submit a payment reference for verification.
   * Handles 200 (immediate result) and 202 (queued requestId).
   */
  async submitVerification(req: VerifyEtSubmitRequest): Promise<VerifyEtResponse> {
    const apiKey = this.getApiKey();

    const payload: Record<string, any> = {
      bank: req.bank,
      settlementAccount: req.settlementAccount,
      reference: req.reference || req.transactionNumber || req.referenceNumber || req.receiptNumber,
    };

    const callApi = async () => {
      return this.http.post('/api/verify', payload, {
        headers: {
          'x-api-key': apiKey,
          'Idempotency-Key': req.idempotencyKey,
        },
      });
    };

    let response;
    try {
      response = await callApi();
    } catch (err: any) {
      // 1 retry on network/socket error only, never on 4xx
      if (!err.response && (err.code === 'ECONNABORTED' || err.code === 'ECONNRESET' || err.code === 'ETIMEDOUT')) {
        this.logger.warn({ msg: 'verify.et network timeout, retrying once' });
        response = await callApi();
      } else {
        throw err;
      }
    }

    if (response.status === 202) {
      return {
        httpStatus: 202,
        requestId: response.data?.requestId || response.data?.id,
      };
    }

    const data = response.data;
    return {
      httpStatus: 200,
      requestId: data.requestId,
      result: {
        requestId: data.requestId,
        status: data.status || 'success',
        verified: data.verified !== undefined ? data.verified : data.status === 'success',
        amount: data.amount,
        currency: data.currency || 'ETB',
        senderName: data.senderName,
        receiverName: data.receiverName,
        receiverAccount: data.receiverAccount,
        referenceNumber: data.referenceNumber,
        timestamp: data.timestamp,
        confirmationHistory: data.confirmationHistory,
        settlementAccountMatch: data.settlementAccountMatch,
      },
    };
  }

  /**
   * Poll for a specific requestId (fallback reconciliation).
   */
  async pollResult(requestId: string): Promise<VerifyEtResult | null> {
    const apiKey = this.getApiKey();
    try {
      const response = await this.http.get(`/api/verify/status/${requestId}`, {
        headers: { 'x-api-key': apiKey },
      });
      return response.data;
    } catch (err: any) {
      if (err.response?.status === 404) {
        return null;
      }
      throw err;
    }
  }

  private getApiKey(): string {
    const key = this.config.get<string>('VERIFY_ET_API_KEY');
    if (!key || key.includes('PLACEHOLDER')) {
      return 'dev_mock_key';
    }
    return key;
  }
}
