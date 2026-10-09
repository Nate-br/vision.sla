import { VerifyEtReconciliationJob } from './verify-et-reconciliation.job';
import { PaymentAttempt, PaymentPurpose } from '../payments/entities/payment-attempt.entity';
import { PaymentStatus, PaymentProvider } from '../common/types/domain.types';
import { VerifyEtClient, VerifyEtResult } from '../verification/verify-et.client';
import { LedgerService } from '../ledger/ledger.service';
import { Repository } from 'typeorm';

describe('VerifyEtReconciliationJob', () => {
  let job: VerifyEtReconciliationJob;
  let paymentRepo: Partial<Repository<PaymentAttempt>>;
  let verifyEtClient: Partial<VerifyEtClient>;
  let ledgerService: Partial<LedgerService>;

  beforeEach(() => {
    paymentRepo = {
      find: jest.fn(),
      save: jest.fn().mockImplementation((p) => Promise.resolve(p)),
    };

    verifyEtClient = {
      pollResult: jest.fn(),
    };

    ledgerService = {
      postDoubleEntry: jest.fn().mockResolvedValue(undefined),
    };

    job = new VerifyEtReconciliationJob(
      paymentRepo as Repository<PaymentAttempt>,
      verifyEtClient as VerifyEtClient,
      ledgerService as LedgerService,
    );
  });

  it('should do nothing when no payments are stuck in VERIFYING', async () => {
    (paymentRepo.find as jest.Mock).mockResolvedValue([]);

    const result = await job.handleReconciliation();
    expect(result.processedCount).toBe(0);
    expect(verifyEtClient.pollResult).not.toHaveBeenCalled();
  });

  it('should reconcile stuck payment and credit when verify.et confirms payment (Plan 4.2 step 4)', async () => {
    const stuckPayment: PaymentAttempt = {
      id: 'pay-stuck-1',
      memberId: 'mem-1',
      paymentProvider: PaymentProvider.TELEBIRR,
      rawReference: 'RA47KL92X',
      normalizedReference: 'RA47KL92X',
      expectedAmountSantim: '50000',
      creditedAmountSantim: null,
      currency: 'ETB',
      purpose: PaymentPurpose.SAVINGS,
      targetLoanId: null,
      status: PaymentStatus.VERIFYING,
      verifyEtRequestId: 'req-verify-99',
      decision: null,
      rejectionReason: null,
      settlementAccount: '0911223344',
      rawVerificationResponse: null,
      createdAt: new Date(Date.now() - 10 * 60 * 1000), // 10 minutes ago
      updatedAt: new Date(Date.now() - 5 * 60 * 1000),  // 5 minutes ago
    };

    (paymentRepo.find as jest.Mock).mockResolvedValue([stuckPayment]);

    const pollResult: VerifyEtResult = {
      requestId: 'req-verify-99',
      status: 'success',
      verified: true,
      amount: 500.0,
      currency: 'ETB',
      settlementAccountMatch: { matched: true, ambiguous: false },
      confirmationHistory: { confirmedBefore: false, confirmationCount: 0 },
      timestamp: new Date().toISOString(),
    };

    (verifyEtClient.pollResult as jest.Mock).mockResolvedValue(pollResult);

    const report = await job.handleReconciliation();

    expect(report.processedCount).toBe(1);
    expect(report.reconciledCount).toBe(1);
    expect(stuckPayment.status).toBe(PaymentStatus.VERIFIED_CREDITED);
    expect(stuckPayment.creditedAmountSantim).toBe('50000');
    expect(ledgerService.postDoubleEntry).toHaveBeenCalledWith(
      expect.objectContaining({
        transactionReferenceId: 'pay-stuck-1',
        amountSantim: '50000',
      }),
    );
  });

  it('should move payments older than 72 hours without result to MANUAL_REVIEW', async () => {
    const expiredPayment: PaymentAttempt = {
      id: 'pay-expired-1',
      memberId: 'mem-1',
      paymentProvider: PaymentProvider.TELEBIRR,
      rawReference: 'OLDREF999',
      normalizedReference: 'OLDREF999',
      expectedAmountSantim: '50000',
      creditedAmountSantim: null,
      currency: 'ETB',
      purpose: PaymentPurpose.SAVINGS,
      targetLoanId: null,
      status: PaymentStatus.VERIFYING,
      verifyEtRequestId: 'req-expired-99',
      decision: null,
      rejectionReason: null,
      settlementAccount: '0911223344',
      rawVerificationResponse: null,
      createdAt: new Date(Date.now() - 75 * 60 * 60 * 1000), // 75 hours ago (> 72h)
      updatedAt: new Date(Date.now() - 5 * 60 * 1000),
    };

    (paymentRepo.find as jest.Mock).mockResolvedValue([expiredPayment]);
    (verifyEtClient.pollResult as jest.Mock).mockResolvedValue(null); // provider still has no result

    const report = await job.handleReconciliation();

    expect(report.processedCount).toBe(1);
    expect(expiredPayment.status).toBe(PaymentStatus.MANUAL_REVIEW);
    expect(expiredPayment.rejectionReason).toBe('VERIFY_ET_TIMEOUT_EXPIRED_72H');
  });
});
