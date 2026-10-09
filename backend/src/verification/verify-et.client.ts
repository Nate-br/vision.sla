/**
 * VerifyEtClient — Phase 3 implementation target.
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
 * Last checked: 2026-10-09 (re-check before each release)
 */
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios, { AxiosInstance } from 'axios';
import { PaymentProvider } from '../common/types/domain.types';

export interface VerifyEtSubmitRequest {
  /** Our internal payment attempt ID — used as Idempotency-Key */
  idempotencyKey: string;
  bank: string;
  /** The receiving account / phone of our organisation */
  settlementAccount: string;
  // Provider-specific fields (one of the following blocks):
  reference?: string;        // CBE, Telebirr
  suffix?: string;           // CBE old FT... refs
  transactionNumber?: string;// Telebirr, M-Pesa
  receiptNumber?: string;    // CBE Birr
  phone?: string;            // CBE Birr
  referenceNumber?: string;  // BoA, Dashen, Awash, Siinqee
  accountSuffix?: string;    // BoA (5-digit)
}

export interface VerifyEtResult {
  requestId: string;
  status: 'success' | string;
  verified: boolean;
  amount?: number;       // minor units? — confirm with docs
  currency?: string;     // should be 'ETB'
  senderName?: string;
  receiverName?: string;
  receiverAccount?: string; // may be masked
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
  /** 200 = done, 202 = queued (wait for webhook or poll) */
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
        // API key injected per-request to avoid logging in axios debug
      },
    });
  }

  /**
   * Submit a payment reference for verification.
   * plan Section 4.2, step 2:
   *  - on 200: result in body
   *  - on 202: queued; store requestId and wait for webhook
   *  - Never retry 4xx. 1 retry on network error only.
   */
  async submitVerification(req: VerifyEtSubmitRequest): Promise<VerifyEtResponse> {
    // TODO(Phase 3): implement with circuit breaker, retry on network error only
    // DO NOT log req.reference, req.transactionNumber, or the API key
    this.logger.log({
      msg: 'verify.et submit',
      idempotencyKey: req.idempotencyKey,
      bank: req.bank,
      // Note: no reference number logged
    });

    throw new Error('VerifyEtClient.submitVerification not yet implemented — Phase 3');
  }

  /**
   * Poll for a specific requestId (fallback if webhook missed).
   * plan Section 4.2, step 4: reconciliation job calls this.
   */
  async pollResult(requestId: string): Promise<VerifyEtResult | null> {
    // TODO(Phase 3): implement
    throw new Error('VerifyEtClient.pollResult not yet implemented — Phase 3');
  }

  private getApiKey(): string {
    const key = this.config.get<string>('VERIFY_ET_API_KEY');
    if (!key || key.includes('PLACEHOLDER')) {
      throw new Error('VERIFY_ET_API_KEY is not configured');
    }
    return key;
  }
}
