import {
  Injectable,
  UnauthorizedException,
  BadRequestException,
  ConflictException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as crypto from 'crypto';
import { WebhookEvent } from './entities/webhook-event.entity';
import { PaymentAttempt } from '../payments/entities/payment-attempt.entity';
import { PaymentStatus } from '../common/types/domain.types';
import { LedgerService } from '../ledger/ledger.service';
import { LedgerAccountType } from '../ledger/entities/ledger-entry.entity';

export const MAX_TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000; // 5 minutes (S-305)

@Injectable()
export class WebhooksService {
  private readonly webhookSecret: string;

  constructor(
    private readonly configService: ConfigService,
    @InjectRepository(WebhookEvent)
    private readonly webhookEventRepo: Repository<WebhookEvent>,
    @InjectRepository(PaymentAttempt)
    private readonly paymentAttemptRepo: Repository<PaymentAttempt>,
    private readonly ledgerService: LedgerService,
  ) {
    this.webhookSecret = this.configService.get<string>(
      'VERIFY_ET_WEBHOOK_SECRET',
      'whsec_dev_test_secret_for_local_development',
    );
  }

  /**
   * Verifies the HMAC-SHA256 signature using constant-time comparison (S-304).
   */
  verifySignature(rawBody: string, signatureHeader: string): boolean {
    if (!signatureHeader || !rawBody) return false;

    // Expected format: t=1690000000,v1=abc123hex... or raw hex
    let signatureToVerify = signatureHeader;
    if (signatureHeader.includes('v1=')) {
      const match = signatureHeader.match(/v1=([a-f0-9]+)/i);
      if (match) {
        signatureToVerify = match[1];
      }
    }

    const computedHmac = crypto
      .createHmac('sha256', this.webhookSecret)
      .update(rawBody)
      .digest('hex');

    try {
      const expectedBuffer = Buffer.from(computedHmac, 'utf8');
      const receivedBuffer = Buffer.from(signatureToVerify, 'utf8');
      if (expectedBuffer.length !== receivedBuffer.length) {
        return false;
      }
      return crypto.timingSafeEqual(expectedBuffer, receivedBuffer);
    } catch {
      return false;
    }
  }

  /**
   * Enforces webhook timestamp tolerance within 5 minutes (S-305).
   */
  validateTimestamp(timestampHeader: string | undefined): boolean {
    if (!timestampHeader) return false;

    const timestampMs = parseInt(timestampHeader, 10) * (timestampHeader.length <= 10 ? 1000 : 1);
    if (isNaN(timestampMs)) return false;

    const now = Date.now();
    return Math.abs(now - timestampMs) <= MAX_TIMESTAMP_TOLERANCE_MS;
  }

  /**
   * Processes the verified webhook event with replay deduplication (S-306).
   */
  async handleWebhook(
    rawBody: string,
    signatureHeader: string,
    timestampHeader: string | undefined,
    eventId: string,
    payload: any,
  ) {
    // 1. Verify HMAC Signature
    if (!this.verifySignature(rawBody, signatureHeader)) {
      throw new UnauthorizedException('Invalid webhook HMAC signature.');
    }

    // 2. Verify Timestamp
    if (timestampHeader && !this.validateTimestamp(timestampHeader)) {
      throw new BadRequestException('Webhook timestamp outside acceptable tolerance window.');
    }

    // 3. Deduplicate on eventId (S-306)
    const existing = await this.webhookEventRepo.findOne({ where: { eventId } });
    if (existing) {
      // Idempotent return: already received and acknowledged
      return { status: 'ALREADY_PROCESSED', eventId };
    }

    const event = this.webhookEventRepo.create({
      eventId,
      eventType: payload?.event || 'verification.completed',
      payload,
      isProcessed: false,
    });
    await this.webhookEventRepo.save(event);

    // 4. Handle Payment Verification Asynchronously
    try {
      await this.processVerificationEvent(payload);
      event.isProcessed = true;
      event.processedAt = new Date();
      await this.webhookEventRepo.save(event);
    } catch (err: any) {
      event.processingError = err.message || 'Unknown processing error';
      await this.webhookEventRepo.save(event);
    }

    return { status: 'SUCCESS', eventId };
  }

  private async processVerificationEvent(payload: any) {
    const requestId = payload?.data?.requestId || payload?.requestId;
    if (!requestId) return;

    const payment = await this.paymentAttemptRepo.findOne({
      where: { verifyEtRequestId: requestId },
    });
    if (!payment) return;

    const status = payload?.data?.status || payload?.status;
    const verifiedAmount = payload?.data?.amount || payload?.amount;

    if (status === 'success' || status === 'COMPLETED') {
      payment.status = PaymentStatus.VERIFIED_CREDITED;
      payment.creditedAmountSantim = (BigInt(Math.round(Number(verifiedAmount) * 100))).toString();
      payment.rawVerificationResponse = payload;
      await this.paymentAttemptRepo.save(payment);

      // Settle in Ledger
      await this.ledgerService.postDoubleEntry({
        transactionReferenceId: payment.id,
        memberId: payment.memberId,
        amountSantim: payment.creditedAmountSantim,
        debitAccount: LedgerAccountType.ASSET_BANK_CLEARING,
        creditAccount:
          payment.purpose === 'LOAN_REPAYMENT'
            ? LedgerAccountType.ASSET_LOAN_RECEIVABLE
            : LedgerAccountType.LIABILITY_MEMBER_SAVINGS,
        description: `Verified Payment via ${payment.paymentProvider} (${payment.normalizedReference})`,
        isLoanRepayment: payment.purpose === 'LOAN_REPAYMENT',
        targetLoanId: payment.targetLoanId || undefined,
        paymentProvider: payment.paymentProvider,
      });
    } else {
      payment.status = PaymentStatus.REJECTED;
      payment.rejectionReason = payload?.data?.message || 'Verification rejected by provider';
      await this.paymentAttemptRepo.save(payment);
    }
  }
}
