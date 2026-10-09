import { LoansService } from './loans.service';
import { Loan, LoanType, LoanStatus } from './entities/loan.entity';
import { LoanProduct } from './entities/loan-product.entity';
import { LoanApplication, LoanApplicationStatus } from './entities/loan-application.entity';
import { LoanInstallment, InstallmentStatus } from './entities/loan-installment.entity';
import { Repository } from 'typeorm';
import { BadRequestException, NotFoundException } from '@nestjs/common';

describe('LoansService', () => {
  let service: LoansService;
  let loanRepo: Partial<Repository<Loan>>;
  let productRepo: Partial<Repository<LoanProduct>>;
  let applicationRepo: Partial<Repository<LoanApplication>>;
  let installmentRepo: Partial<Repository<LoanInstallment>>;

  const mockProduct: LoanProduct = {
    id: 'prod-1',
    code: 'BUSINESS_MICRO',
    name: 'Business Micro Loan',
    description: 'Working capital for small businesses',
    minAmountSantim: '100000', // 1,000 ETB
    maxAmountSantim: '10000000', // 100,000 ETB
    interestRateBps: 1200, // 12% p.a.
    minTenureMonths: 3,
    maxTenureMonths: 24,
    processingFeeBps: 100, // 1%
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  };

  beforeEach(() => {
    loanRepo = {
      findOne: jest.fn(),
      find: jest.fn(),
      save: jest.fn().mockImplementation((l) => Promise.resolve(l)),
    };
    productRepo = {
      findOne: jest.fn().mockResolvedValue(mockProduct),
      find: jest.fn().mockResolvedValue([mockProduct]),
    };
    applicationRepo = {
      create: jest.fn().mockImplementation((dto) => ({ id: 'app-1', ...dto })),
      save: jest.fn().mockImplementation((a) => Promise.resolve(a)),
      find: jest.fn(),
    };
    installmentRepo = {
      find: jest.fn(),
      findOne: jest.fn(),
      save: jest.fn().mockImplementation((i) => Promise.resolve(i)),
      count: jest.fn(),
    };

    service = new LoansService(
      loanRepo as Repository<Loan>,
      productRepo as Repository<LoanProduct>,
      applicationRepo as Repository<LoanApplication>,
      installmentRepo as Repository<LoanInstallment>,
    );
  });

  it('should calculate loan installments and interest accurately with integer santim arithmetic', async () => {
    const calc = await service.calculateLoan({
      productId: 'prod-1',
      amountSantim: '1200000', // 12,000.00 ETB
      termMonths: 12,
    });

    // 12,000 ETB at 12% p.a. for 1 year = 1,440 ETB interest = 144,000 santim
    expect(calc.totalInterestSantim).toBe('144000');
    expect(calc.totalDueSantim).toBe('1344000'); // 13,440.00 ETB
    expect(calc.monthlyInstallmentSantim).toBe('112000'); // 1,120.00 ETB/mo
    expect(calc.schedulePreview.length).toBe(12);
  });

  it('should reject loan application outside allowed product limits', async () => {
    await expect(
      service.applyForLoan('mem-1', {
        productId: 'prod-1',
        requestedAmountSantim: '50000', // 500 ETB is below 1,000 min
        termMonths: 6,
        purpose: 'Inventory purchase',
      }),
    ).rejects.toThrow(BadRequestException);

    await expect(
      service.applyForLoan('mem-1', {
        productId: 'prod-1',
        requestedAmountSantim: '5000000',
        termMonths: 36, // 36 months is above 24 max
        purpose: 'Inventory purchase',
      }),
    ).rejects.toThrow(BadRequestException);
  });

  describe('Waterfall Repayment Auto-Allocation Engine (S-401)', () => {
    let mockLoan: Loan;
    let inst1: LoanInstallment;
    let inst2: LoanInstallment;

    beforeEach(() => {
      mockLoan = {
        id: 'loan-1',
        memberId: 'mem-1',
        loanType: LoanType.BUSINESS,
        productName: 'Business Micro Loan',
        principalSantim: '1000000', // 10,000 ETB
        interestRateBps: 1200,
        interestAmountSantim: '120000',
        totalDueSantim: '1120000',
        balanceRemainingSantim: '1120000',
        status: LoanStatus.ACTIVE,
        termMonths: 2,
        monthlyInstallmentSantim: '560000',
        nextPaymentDueDate: '2026-11-01',
        totalInstallments: 2,
        paidInstallments: 0,
        disbursedAt: new Date(),
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      inst1 = {
        id: 'inst-1',
        loanId: 'loan-1',
        installmentNumber: 1,
        dueDate: '2026-11-01',
        principalDueSantim: '500000',
        interestDueSantim: '60000',
        totalDueSantim: '560000',
        principalPaidSantim: '0',
        interestPaidSantim: '0',
        totalPaidSantim: '0',
        status: InstallmentStatus.PENDING,
        paidAt: null,
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      inst2 = {
        id: 'inst-2',
        loanId: 'loan-1',
        installmentNumber: 2,
        dueDate: '2026-12-01',
        principalDueSantim: '500000',
        interestDueSantim: '60000',
        totalDueSantim: '560000',
        principalPaidSantim: '0',
        interestPaidSantim: '0',
        totalPaidSantim: '0',
        status: InstallmentStatus.PENDING,
        paidAt: null,
        createdAt: new Date(),
        updatedAt: new Date(),
      };
    });

    it('should allocate partial payment to interest first, then principal (S-401)', async () => {
      (loanRepo.findOne as jest.Mock).mockResolvedValue(mockLoan);
      (installmentRepo.find as jest.Mock).mockResolvedValue([inst1]);
      (installmentRepo.count as jest.Mock).mockResolvedValue(0);

      // Pay 100,000 santim (1,000 ETB): interest due is 60,000, remaining 40,000 to principal
      const result = await service.allocateRepayment('loan-1', '100000', 'mem-1');

      expect(result.allocatedSantim).toBe('100000');
      expect(result.remainingOverpaymentSantim).toBe('0');
      expect(inst1.interestPaidSantim).toBe('60000');
      expect(inst1.principalPaidSantim).toBe('40000');
      expect(inst1.totalPaidSantim).toBe('100000');
      expect(inst1.status).toBe(InstallmentStatus.PARTIALLY_PAID);
    });

    it('should waterfall across multiple installments sequentially (S-401)', async () => {
      (loanRepo.findOne as jest.Mock).mockResolvedValue(mockLoan);
      (installmentRepo.find as jest.Mock).mockResolvedValue([inst1, inst2]);
      (installmentRepo.count as jest.Mock).mockResolvedValue(1);

      // Pay 800,000 santim:
      // Inst 1: 60,000 interest + 500,000 principal = 560,000 (FULLY PAID)
      // Remaining: 240,000 -> Inst 2: 60,000 interest + 180,000 principal (PARTIALLY PAID)
      const result = await service.allocateRepayment('loan-1', '800000', 'mem-1');

      expect(result.allocatedSantim).toBe('800000');
      expect(result.remainingOverpaymentSantim).toBe('0');
      expect(inst1.status).toBe(InstallmentStatus.PAID);
      expect(inst2.status).toBe(InstallmentStatus.PARTIALLY_PAID);
      expect(inst2.interestPaidSantim).toBe('60000');
      expect(inst2.principalPaidSantim).toBe('180000');
    });

    it('should mark loan as PAID_OFF and return excess overpayment santim (S-403)', async () => {
      (loanRepo.findOne as jest.Mock).mockResolvedValue(mockLoan);
      (installmentRepo.find as jest.Mock).mockResolvedValue([inst1, inst2]);
      (installmentRepo.count as jest.Mock).mockResolvedValue(2);

      // Total due is 1,120,000 santim. Member pays 1,200,000 santim (80,000 overpayment)
      const result = await service.allocateRepayment('loan-1', '1200000', 'mem-1');

      expect(result.allocatedSantim).toBe('1120000');
      expect(result.remainingOverpaymentSantim).toBe('80000'); // 800 ETB excess returned
      expect(result.loanStatus).toBe(LoanStatus.PAID_OFF);
      expect(result.newBalanceRemainingSantim).toBe('0');
    });
  });
});
