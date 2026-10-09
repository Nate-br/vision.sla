import { SavingsService } from './savings.service';
import { SavingsAccount } from './entities/savings-account.entity';
import { SavingsTransaction, TransactionType, TransactionStatus } from './entities/savings-transaction.entity';
import { Repository } from 'typeorm';

describe('SavingsService', () => {
  let service: SavingsService;
  let accountRepo: Partial<Repository<SavingsAccount>>;
  let transactionRepo: Partial<Repository<SavingsTransaction>>;

  beforeEach(() => {
    accountRepo = {
      findOne: jest.fn(),
      create: jest.fn().mockImplementation((dto) => ({ id: 'acc-1', ...dto })),
      save: jest.fn().mockImplementation((acc) => Promise.resolve(acc)),
    };

    transactionRepo = {
      findAndCount: jest.fn(),
    };

    service = new SavingsService(
      accountRepo as Repository<SavingsAccount>,
      transactionRepo as Repository<SavingsTransaction>,
    );
  });

  it('should auto-initialize a savings account with 0 santim on first summary request', async () => {
    (accountRepo.findOne as jest.Mock).mockResolvedValue(null);

    const summary = await service.getSummary('mem-123');
    expect(summary.totalSavingsSantim).toBe('0');
    expect(summary.availableSantim).toBe('0');
    expect(summary.currency).toBe('ETB');
    expect(accountRepo.create).toHaveBeenCalled();
    expect(accountRepo.save).toHaveBeenCalled();
  });

  it('should return existing account summary formatted in minor santim units', async () => {
    const existingAccount: SavingsAccount = {
      id: 'acc-1',
      memberId: 'mem-123',
      accountNumber: 'VBL-SAV-100200',
      totalSavingsSantim: '1500000', // 15,000.00 ETB
      availableSantim: '1450000',
      frozenSantim: '50000',
      totalInterestEarnedSantim: '25000',
      currency: 'ETB',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    (accountRepo.findOne as jest.Mock).mockResolvedValue(existingAccount);

    const summary = await service.getSummary('mem-123');
    expect(summary.accountNumber).toBe('VBL-SAV-100200');
    expect(summary.totalSavingsSantim).toBe('1500000');
    expect(summary.availableSantim).toBe('1450000');
    expect(summary.frozenSantim).toBe('50000');
  });

  it('should return paginated transaction history', async () => {
    const mockTx: SavingsTransaction = {
      id: 'tx-1',
      memberId: 'mem-123',
      savingsAccountId: 'acc-1',
      transactionType: TransactionType.DEPOSIT,
      amountSantim: '500000',
      balanceBeforeSantim: '0',
      balanceAfterSantim: '500000',
      reference: 'TX-REF-01',
      paymentProvider: 'TELEBIRR',
      status: TransactionStatus.COMPLETED,
      description: 'Monthly contribution',
      createdAt: new Date(),
    };

    (transactionRepo.findAndCount as jest.Mock).mockResolvedValue([[mockTx], 1]);

    const result = await service.getTransactions('mem-123', 1, 20);
    expect(result.data).toHaveLength(1);
    expect(result.total).toBe(1);
    expect(result.data[0].amountSantim).toBe('500000');
    expect(result.data[0].paymentProvider).toBe('TELEBIRR');
  });
});
