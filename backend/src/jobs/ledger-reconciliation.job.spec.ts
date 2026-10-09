import { LedgerReconciliationJob } from './ledger-reconciliation.job';
import { LedgerEntry, LedgerAccountType, LedgerEntryType } from '../ledger/entities/ledger-entry.entity';
import { Repository } from 'typeorm';

describe('LedgerReconciliationJob', () => {
  let job: LedgerReconciliationJob;
  let ledgerRepo: Partial<Repository<LedgerEntry>>;

  beforeEach(() => {
    ledgerRepo = {
      find: jest.fn(),
    };
    job = new LedgerReconciliationJob(ledgerRepo as Repository<LedgerEntry>);
  });

  it('should verify balanced debits and credits correctly (S-511)', async () => {
    (ledgerRepo.find as jest.Mock).mockResolvedValue([
      {
        id: 'entry-1',
        transactionReferenceId: 'tx-1',
        accountType: LedgerAccountType.ASSET_BANK_CLEARING,
        entryType: LedgerEntryType.DEBIT,
        currency: 'ETB',
        memberId: 'mem-1',
        amountSantim: '50000',
        description: 'Deposit',
        createdAt: new Date(),
      } as unknown as LedgerEntry,
      {
        id: 'entry-2',
        transactionReferenceId: 'tx-2',
        accountType: LedgerAccountType.LIABILITY_MEMBER_SAVINGS,
        entryType: LedgerEntryType.CREDIT,
        currency: 'ETB',
        memberId: 'mem-1',
        amountSantim: '150000',
        description: 'Loan Repayment',
        createdAt: new Date(),
      } as unknown as LedgerEntry,
    ]);

    const report = await job.handleNightlyReconciliation();

    expect(report.isBalanced).toBe(true);
    expect(report.totalDebitSantim).toBe('200000');
    expect(report.totalCreditSantim).toBe('200000');
    expect(report.discrepancySantim).toBe('0');
    expect(report.totalEntriesCount).toBe(2);
  });
});
