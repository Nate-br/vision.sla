import { LoansService } from './loans.service';
import { Loan, LoanType, LoanStatus } from './entities/loan.entity';
import { Repository } from 'typeorm';

describe('LoansService', () => {
  let service: LoansService;
  let loanRepo: Partial<Repository<Loan>>;

  beforeEach(() => {
    loanRepo = {
      findOne: jest.fn(),
      find: jest.fn(),
    };
    service = new LoansService(loanRepo as Repository<Loan>);
  });

  it('should return hasActiveLoan: false when member has no loans', async () => {
    (loanRepo.findOne as jest.Mock).mockResolvedValue(null);

    const summary = await service.getSummary('mem-123');
    expect(summary.hasActiveLoan).toBe(false);
    expect(summary.activeLoan).toBeNull();
  });

  it('should return active loan summary with santim values and installment count', async () => {
    const activeLoan: Loan = {
      id: 'loan-1',
      memberId: 'mem-123',
      loanType: LoanType.BUSINESS,
      productName: 'SLA Small Business Loan',
      principalSantim: '10000000', // 100,000.00 ETB
      interestRateBps: 1000, // 10.00%
      interestAmountSantim: '1000000',
      totalDueSantim: '11000000',
      balanceRemainingSantim: '6000000',
      status: LoanStatus.ACTIVE,
      termMonths: 12,
      monthlyInstallmentSantim: '916666',
      nextPaymentDueDate: '2026-11-01',
      totalInstallments: 12,
      paidInstallments: 5,
      disbursedAt: new Date(),
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    (loanRepo.findOne as jest.Mock).mockResolvedValue(activeLoan);

    const summary = await service.getSummary('mem-123');
    expect(summary.hasActiveLoan).toBe(true);
    expect(summary.activeLoan?.productName).toBe('SLA Small Business Loan');
    expect(summary.activeLoan?.totalDueSantim).toBe('11000000');
    expect(summary.activeLoan?.paidInstallments).toBe(5);
  });
});
