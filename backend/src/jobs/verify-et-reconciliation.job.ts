import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThanOrEqual, IsNull, Not } from 'typeorm';
import { PaymentAttempt, PaymentPurpose } from '../payments/entities/payment-attempt.entity';
import { PaymentStatus } from '../common/types/domain.types';
import { VerifyEtClient } from '../verification/verify-et.client';
import { LedgerService } from '../ledger/ledger.service';
import { LedgerAccountType } from '../ledger/entities/ledger-entry.entity';
import { decidePaymentOutcome, DecisionContext } from '../verification/decision-engine';

@Injectable()
export class VerifyEtReconciliationJob {
  private readonly logger = new Logger(VerifyEtReconciliationJob.name);

  constructor(
    @InjectRepository(PaymentAttempt)
    private readonly paymentRepo: Repository<PaymentAttempt>,
    private readonly verifyEtClient: VerifyEtClient,
    private readonly ledgerService: LedgerService,
  ) {}

  /**
   * Reconciliation job every 5 minutes (Plan Section 4.2, step 4).
   * Polls verify.et for any attempt stuck in VERIFYING > 3 minutes.
   */
  @Cron(CronExpression.EVERY_5_MINUTES)
  async handleReconciliation() {
    this.logger.log('Starting 5-minute verify.et reconciliation sweep...');

    const thresholdTime = new Date(Date.now() - 3 * 60 * 1000); // 3 minutes ago
    const stuckPayments = await this.paymentRepo.find({
      where: {
        status: PaymentStatus.VERIFYING,
        verifyEtRequestId: Not(IsNull()),
        updatedAt: LessThanOrEqual(thresholdTime),
      },
      take: 50,
    });

    if (stuckPayments.length === 0) {
      this.logger.debug('No stuck payments pending reconciliation.');
      return { processedCount: 0 };
    }

    this.logger.log(`Found ${stuckPayments.length} payments stuck in VERIFYING > 3m. Polling verify.et...`);

    let reconciledCount = 0;

    for (const payment of stuckPayments) {
      if (!payment.verifyEtRequestId) continue;

      try {
        const result = await this.verifyEtClient.pollResult(payment.verifyEtRequestId);
        if (!result) {
          // Check if stuck > 72 hours -> escalate to manual review
          const ageMs = Date.now() - payment.createdAt.getTime();
          if (ageMs > 72 * 60 * 60 * 1000) {
            payment.status = PaymentStatus.MANUAL_REVIEW;
            payment.rejectionReason = 'VERIFY_ET_TIMEOUT_EXPIRED_72H';
            await this.paymentRepo.save(payment);
            this.logger.warn(`Payment ${payment.id} exceeded 72h verification window. Moved to MANUAL_REVIEW.`);
          }
          continue;
        }

        // Run decision engine
        const maxReceiptAgeDays = 30;
        const invoiceDateIso = new Date(Date.now() - maxReceiptAgeDays * 24 * 60 * 60 * 1000).toISOString();

        const decision = decidePaymentOutcome({
          result,
          expectedAmountSantim: BigInt(payment.expectedAmountSantim),
          allowFlexibleAmount: payment.purpose !== PaymentPurpose.LOAN_REPAYMENT,
          invoiceDateIso,
          maxReceiptAgeDays,
          alreadyCreditedInDb: false,
          memberLimitsExceeded: false,
          ourAccountKeys: [payment.settlementAccount || '0911000000'],
        });

        if (decision.action === 'CREDIT') {
          const creditedSantim = BigInt(Math.round((result.amount || 0) * 100)).toString();
          payment.status = PaymentStatus.VERIFIED_CREDITED;
          payment.creditedAmountSantim = creditedSantim;
          payment.rawVerificationResponse = result as any;
          payment.decision = 'CREDIT';
          await this.paymentRepo.save(payment);

          // Post double-entry ledger settlement
          await this.ledgerService.postDoubleEntry({
            transactionReferenceId: payment.id,
            memberId: payment.memberId,
            amountSantim: creditedSantim,
            debitAccount: LedgerAccountType.ASSET_BANK_CLEARING,
            creditAccount:
              payment.purpose === PaymentPurpose.LOAN_REPAYMENT
                ? LedgerAccountType.ASSET_LOAN_RECEIVABLE
                : LedgerAccountType.LIABILITY_MEMBER_SAVINGS,
            description: `Reconciled ${payment.paymentProvider} payment (${payment.normalizedReference})`,
            isLoanRepayment: payment.purpose === PaymentPurpose.LOAN_REPAYMENT,
            targetLoanId: payment.targetLoanId || undefined,
            paymentProvider: payment.paymentProvider,
          });

          this.logger.log(`Payment ${payment.id} successfully reconciled and credited!`);
          reconciledCount++;
        } else if (decision.action === 'REJECT') {
          payment.status = PaymentStatus.REJECTED;
          payment.rejectionReason = decision.reasonCode;
          payment.decision = 'REJECT';
          await this.paymentRepo.save(payment);
          reconciledCount++;
        } else if (decision.action === 'MANUAL_REVIEW') {
          payment.status = PaymentStatus.MANUAL_REVIEW;
          payment.rejectionReason = decision.reasonCode;
          payment.decision = 'MANUAL_REVIEW';
          await this.paymentRepo.save(payment);
          reconciledCount++;
        }
      } catch (err: any) {
        this.logger.error(`Error polling verify.et for payment ${payment.id}: ${err.message}`);
      }
    }

    return { processedCount: stuckPayments.length, reconciledCount };
  }
}
