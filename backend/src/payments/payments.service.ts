import {
  Injectable,
  ConflictException,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaymentAttempt, PaymentPurpose } from './entities/payment-attempt.entity';
import { OrgReceivingAccount } from './entities/org-receiving-account.entity';
import { PaymentProvider, PaymentStatus } from '../common/types/domain.types';
import { SubmitPaymentDto } from './dto/submit-payment.dto';
import { VerifyEtClient } from '../verification/verify-et.client';
import { decidePaymentOutcome, DecisionContext } from '../verification/decision-engine';
import { LedgerService } from '../ledger/ledger.service';
import { LedgerAccountType } from '../ledger/entities/ledger-entry.entity';

@Injectable()
export class PaymentsService {
  constructor(
    @InjectRepository(PaymentAttempt)
    private readonly paymentRepo: Repository<PaymentAttempt>,
    @InjectRepository(OrgReceivingAccount)
    private readonly orgAccountRepo: Repository<OrgReceivingAccount>,
    private readonly verifyEtClient: VerifyEtClient,
    private readonly ledgerService: LedgerService,
  ) {}

  /**
   * Normalizes receipt reference codes: uppercase, removes dashes and whitespace.
   */
  normalizeReference(raw: string): string {
    return raw.trim().toUpperCase().replace(/[\s\-]/g, '');
  }

  /**
   * Submits a payment receipt for verification and settlement.
   * Enforces zero duplicate reuse (S-301) and passes settlementAccount (S-302).
   */
  async submitPayment(memberId: string, dto: SubmitPaymentDto) {
    const normalizedRef = this.normalizeReference(dto.reference);

    // 1. Duplicate check (S-301)
    const existing = await this.paymentRepo.findOne({
      where: {
        paymentProvider: dto.provider,
        normalizedReference: normalizedRef,
      },
    });

    if (existing && existing.status === PaymentStatus.VERIFIED_CREDITED) {
      throw new ConflictException(
        `This receipt reference (${normalizedRef}) has already been credited. Duplicate reuse is prohibited.`,
      );
    }

    // 2. Fetch organisation receiving account for this provider (S-302)
    const orgAccount = await this.orgAccountRepo.findOne({
      where: { provider: dto.provider, isActive: true },
    });
    const settlementAccount = orgAccount ? orgAccount.accountNumberOrPhone : '0911000000';

    // 3. Create or reuse payment attempt record
    let attempt = existing;
    if (!attempt) {
      attempt = this.paymentRepo.create({
        memberId,
        paymentProvider: dto.provider,
        rawReference: dto.reference,
        normalizedReference: normalizedRef,
        expectedAmountSantim: dto.expectedAmountSantim,
        currency: 'ETB',
        purpose: dto.purpose || PaymentPurpose.SAVINGS,
        targetLoanId: dto.targetLoanId || null,
        status: PaymentStatus.SUBMITTED,
        settlementAccount,
      });
      await this.paymentRepo.save(attempt);
    }

    // 4. Call verify.et client (handles 200 and 202)
    const verifyResponse = await this.verifyEtClient.submitVerification({
      idempotencyKey: attempt.id,
      bank: dto.provider,
      reference: normalizedRef,
      settlementAccount,
    });

    attempt.verifyEtRequestId = verifyResponse.requestId || null;

    if (verifyResponse.httpStatus === 202 || !verifyResponse.result) {
      // Asynchronous 202: provider pending confirmation
      attempt.status = PaymentStatus.VERIFYING;
      await this.paymentRepo.save(attempt);

      return {
        paymentId: attempt.id,
        status: PaymentStatus.VERIFYING,
        provider: attempt.paymentProvider,
        reference: normalizedRef,
        verifyEtRequestId: attempt.verifyEtRequestId,
        message: 'Payment submitted for verification. Provider is confirming.',
      };
    }

    const verificationResult = verifyResponse.result;
    attempt.rawVerificationResponse = verificationResult as any;

    const maxReceiptAgeDays = 30;
    const invoiceDateIso = new Date(Date.now() - maxReceiptAgeDays * 24 * 60 * 60 * 1000).toISOString();

    const decisionContext: DecisionContext = {
      result: verificationResult,
      expectedAmountSantim: BigInt(dto.expectedAmountSantim),
      allowFlexibleAmount: dto.purpose !== PaymentPurpose.LOAN_REPAYMENT,
      invoiceDateIso,
      maxReceiptAgeDays,
      alreadyCreditedInDb: false,
      memberLimitsExceeded: false,
      ourAccountKeys: [settlementAccount],
    };

    const decision = decidePaymentOutcome(decisionContext);
    attempt.decision = decision.action;

    if (decision.action === 'CREDIT') {
      const creditedSantim = BigInt(Math.round((verificationResult.amount || 0) * 100)).toString();
      attempt.status = PaymentStatus.VERIFIED_CREDITED;
      attempt.creditedAmountSantim = creditedSantim;
      await this.paymentRepo.save(attempt);

      // Post atomic double-entry ledger settlement
      await this.ledgerService.postDoubleEntry({
        transactionReferenceId: attempt.id,
        memberId: attempt.memberId,
        amountSantim: creditedSantim,
        debitAccount: LedgerAccountType.ASSET_BANK_CLEARING,
        creditAccount:
          attempt.purpose === PaymentPurpose.LOAN_REPAYMENT
            ? LedgerAccountType.ASSET_LOAN_RECEIVABLE
            : LedgerAccountType.LIABILITY_MEMBER_SAVINGS,
        description: `Verified ${dto.provider} payment (${normalizedRef})`,
        isLoanRepayment: attempt.purpose === PaymentPurpose.LOAN_REPAYMENT,
        targetLoanId: attempt.targetLoanId || undefined,
        paymentProvider: attempt.paymentProvider,
      });

      return {
        paymentId: attempt.id,
        status: PaymentStatus.VERIFIED_CREDITED,
        provider: attempt.paymentProvider,
        reference: normalizedRef,
        amountSantim: creditedSantim,
        message: 'Payment verified and credited successfully.',
      };
    } else if (decision.action === 'PENDING_RETRY') {
      // Async retry / pending
      attempt.status = PaymentStatus.PENDING_RETRY;
      attempt.rejectionReason = decision.reason;
      await this.paymentRepo.save(attempt);

      return {
        paymentId: attempt.id,
        status: PaymentStatus.PENDING_RETRY,
        provider: attempt.paymentProvider,
        reference: normalizedRef,
        verifyEtRequestId: verificationResult.requestId,
        message: 'Payment verification pending retry.',
      };
    } else if (decision.action === 'MANUAL_REVIEW') {
      attempt.status = PaymentStatus.MANUAL_REVIEW;
      attempt.rejectionReason = decision.reasonCode;
      await this.paymentRepo.save(attempt);

      return {
        paymentId: attempt.id,
        status: PaymentStatus.MANUAL_REVIEW,
        provider: attempt.paymentProvider,
        reference: normalizedRef,
        reason: decision.reasonCode,
        message: 'Payment flagged for manual review by our operations team.',
      };
    } else {
      // REJECT
      attempt.status = PaymentStatus.REJECTED;
      attempt.rejectionReason = decision.reasonCode;
      await this.paymentRepo.save(attempt);

      return {
        paymentId: attempt.id,
        status: PaymentStatus.REJECTED,
        provider: attempt.paymentProvider,
        reference: normalizedRef,
        reason: decision.reasonCode,
        message: `Payment rejected: ${decision.reasonCode}`,
      };
    }
  }

  /**
   * Returns current status of a payment attempt.
   */
  async getPaymentStatus(paymentId: string, memberId: string) {
    const payment = await this.paymentRepo.findOne({
      where: { id: paymentId, memberId },
    });
    if (!payment) {
      throw new NotFoundException('Payment record not found.');
    }
    return payment;
  }

  /**
   * Returns active published receiving accounts for all supported providers.
   */
  async getReceivingAccounts() {
    return this.orgAccountRepo.find({ where: { isActive: true } });
  }
}
