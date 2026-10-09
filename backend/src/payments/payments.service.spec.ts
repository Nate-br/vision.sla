import { PaymentsService } from './payments.service';
import { PaymentAttempt, PaymentPurpose } from './entities/payment-attempt.entity';
import { OrgReceivingAccount } from './entities/org-receiving-account.entity';
import { PaymentProvider, PaymentStatus } from '../common/types/domain.types';
import { VerifyEtClient } from '../verification/verify-et.client';
import { LedgerService } from '../ledger/ledger.service';
import { Repository } from 'typeorm';
import { ConflictException } from '@nestjs/common';

describe('PaymentsService', () => {
  let service: PaymentsService;
  let paymentRepo: Partial<Repository<PaymentAttempt>>;
  let orgAccountRepo: Partial<Repository<OrgReceivingAccount>>;
  let verifyEtClient: Partial<VerifyEtClient>;
  let ledgerService: Partial<LedgerService>;

  beforeEach(() => {
    paymentRepo = {
      findOne: jest.fn(),
      create: jest.fn().mockImplementation((dto) => ({ id: 'pay-1', ...dto })),
      save: jest.fn().mockImplementation((p) => Promise.resolve(p)),
    };

    orgAccountRepo = {
      findOne: jest.fn().mockResolvedValue({
        id: 'org-1',
        provider: PaymentProvider.TELEBIRR,
        accountNumberOrPhone: '0911223344',
        accountHolderName: 'Vision For A Better Life SLA',
        isActive: true,
      } as OrgReceivingAccount),
    };

    verifyEtClient = {
      submitVerification: jest.fn(),
    };

    ledgerService = {
      postDoubleEntry: jest.fn().mockResolvedValue(undefined),
    };

    service = new PaymentsService(
      paymentRepo as Repository<PaymentAttempt>,
      orgAccountRepo as Repository<OrgReceivingAccount>,
      verifyEtClient as VerifyEtClient,
      ledgerService as LedgerService,
    );
  });

  it('should normalize reference code by stripping spaces, dashes and converting to uppercase', () => {
    expect(service.normalizeReference('  ra-47 kl 92 x ')).toBe('RA47KL92X');
    expect(service.normalizeReference('cbe-98-112-a')).toBe('CBE98112A');
  });

  it('should reject already credited duplicate receipt with 409 Conflict (S-301)', async () => {
    const creditedPayment: PaymentAttempt = {
      id: 'pay-prev',
      memberId: 'mem-1',
      paymentProvider: PaymentProvider.TELEBIRR,
      rawReference: 'RA47KL92X',
      normalizedReference: 'RA47KL92X',
      expectedAmountSantim: '50000',
      creditedAmountSantim: '50000',
      currency: 'ETB',
      purpose: PaymentPurpose.SAVINGS,
      targetLoanId: null,
      status: PaymentStatus.VERIFIED_CREDITED,
      verifyEtRequestId: 'req-1',
      decision: 'CREDIT',
      rejectionReason: null,
      settlementAccount: '0911223344',
      rawVerificationResponse: null,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    (paymentRepo.findOne as jest.Mock).mockResolvedValue(creditedPayment);

    await expect(
      service.submitPayment('mem-1', {
        provider: PaymentProvider.TELEBIRR,
        reference: ' ra-47kl92x ',
        expectedAmountSantim: '50000',
      }),
    ).rejects.toThrow(ConflictException);
  });

  it('should verify and credit valid payment immediately and post to ledger (Sync 200)', async () => {
    (paymentRepo.findOne as jest.Mock).mockResolvedValue(null);

    (verifyEtClient.submitVerification as jest.Mock).mockResolvedValue({
      httpStatus: 200,
      requestId: 'req-987',
      result: {
        status: 'success',
        verified: true,
        amount: 500.0,
        currency: 'ETB',
        provider: 'Telebirr',
        transactionNumber: 'RA47KL92X',
        settlementAccountMatch: { matched: true, ambiguous: false },
        confirmationHistory: { confirmedBefore: false, confirmationCount: 0 },
        timestamp: new Date().toISOString(),
        payer: 'Test Member',
      },
    });

    const response = await service.submitPayment('mem-1', {
      provider: PaymentProvider.TELEBIRR,
      reference: 'RA47KL92X',
      expectedAmountSantim: '50000',
      purpose: PaymentPurpose.SAVINGS,
    });

    expect(response.status).toBe(PaymentStatus.VERIFIED_CREDITED);
    expect(response.amountSantim).toBe('50000');
    expect(ledgerService.postDoubleEntry).toHaveBeenCalled();
  });
});
