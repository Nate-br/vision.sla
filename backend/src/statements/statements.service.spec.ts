import { StatementsService } from './statements.service';
import { SavingsAccount } from '../savings/entities/savings-account.entity';
import { SavingsTransaction, TransactionType, TransactionStatus } from '../savings/entities/savings-transaction.entity';
import { Loan, LoanType, LoanStatus } from '../loans/entities/loan.entity';
import { CustomerProfile } from '../members/entities/customer-profile.entity';
import { Repository } from 'typeorm';
import { BadRequestException } from '@nestjs/common';

describe('StatementsService', () => {
  let service: StatementsService;
  let accountRepo: Partial<Repository<SavingsAccount>>;
  let transactionRepo: Partial<Repository<SavingsTransaction>>;
  let loanRepo: Partial<Repository<Loan>>;
  let profileRepo: Partial<Repository<CustomerProfile>>;

  beforeEach(() => {
    accountRepo = {
      findOne: jest.fn().mockResolvedValue({
        id: 'acc-1',
        memberId: 'mem-1',
        accountNumber: 'VBL-SAV-987654',
        totalSavingsSantim: '2500000',
        availableSantim: '2400000',
        frozenSantim: '100000',
        totalInterestEarnedSantim: '15000',
        currency: 'ETB',
        isActive: true,
      } as SavingsAccount),
    };

    transactionRepo = {
      find: jest.fn().mockResolvedValue([
        {
          id: 'tx-1',
          memberId: 'mem-1',
          transactionType: TransactionType.DEPOSIT,
          amountSantim: '50000',
          paymentProvider: 'TELEBIRR',
          status: TransactionStatus.COMPLETED,
          description: 'Monthly regular savings',
          createdAt: new Date('2026-10-01T10:00:00Z'),
        } as SavingsTransaction,
        {
          id: 'tx-2',
          memberId: 'mem-1',
          transactionType: TransactionType.DEPOSIT,
          amountSantim: '100000',
          paymentProvider: 'CBE',
          status: TransactionStatus.COMPLETED,
          description: 'Voluntary deposit',
          createdAt: new Date('2026-10-05T14:30:00Z'),
        } as SavingsTransaction,
      ]),
    };

    loanRepo = {
      findOne: jest.fn().mockResolvedValue({
        id: 'loan-1',
        memberId: 'mem-1',
        principalSantim: '5000000',
        balanceRemainingSantim: '3500000',
        status: LoanStatus.ACTIVE,
      } as Loan),
    };

    profileRepo = {
      findOne: jest.fn().mockResolvedValue({
        id: 'prof-1',
        memberId: 'mem-1',
        fullName: 'Abebe Bikila',
      } as unknown as CustomerProfile),
    };

    service = new StatementsService(
      accountRepo as Repository<SavingsAccount>,
      transactionRepo as Repository<SavingsTransaction>,
      loanRepo as Repository<Loan>,
      profileRepo as Repository<CustomerProfile>,
    );
  });

  it('should sanitize control characters and whitespace from input text (S-402)', () => {
    expect(service.sanitizeText('Hello\x00\x1FWorld\x7F')).toBe('HelloWorld');
    expect(service.sanitizeText('  Abebe Bikila  ')).toBe('Abebe Bikila');
    expect(service.sanitizeText(null)).toBe('');
  });

  it('should validate date ranges and reject invalid or inverted dates', () => {
    expect(() => service.validateDateRange('2026-10-15', '2026-10-01')).toThrow(BadRequestException);
    expect(() => service.validateDateRange('invalid-date', '2026-10-01')).toThrow(BadRequestException);

    // More than 365 days window rejected
    expect(() => service.validateDateRange('2024-01-01', '2026-01-01')).toThrow(BadRequestException);

    // Valid range
    const { from, to } = service.validateDateRange('2026-08-01', '2026-10-01');
    expect(from).toBeInstanceOf(Date);
    expect(to).toBeInstanceOf(Date);
  });

  it('should aggregate financial statement numbers and transactions correctly', async () => {
    const data = await service.getStatementData('mem-1', '2026-10-01', '2026-10-09');

    expect(data.memberName).toBe('Abebe Bikila');
    expect(data.accountNumber).toBe('VBL-SAV-987654');
    expect(data.currentSavingsSantim).toBe('2500000');
    expect(data.totalDepositedSantim).toBe('150000'); // 50,000 + 100,000
    expect(data.activeLoanRemainingSantim).toBe('3500000');
    expect(data.transactionsCount).toBe(2);
  });

  it('should generate valid PDF statement buffer starting with %PDF- header (S-402)', async () => {
    const pdfBuffer = await service.generatePdfStatement('mem-1', '2026-10-01', '2026-10-09');

    expect(pdfBuffer).toBeInstanceOf(Buffer);
    expect(pdfBuffer.length).toBeGreaterThan(500);

    // PDF magic bytes header check
    const header = pdfBuffer.slice(0, 5).toString('ascii');
    expect(header).toBe('%PDF-');
  });
});
