import { Injectable, BadRequestException } from '@nestjs/common';
import { DataSource } from 'typeorm';
import { LedgerEntry, LedgerAccountType, LedgerEntryType } from './entities/ledger-entry.entity';
import { SavingsAccount } from '../savings/entities/savings-account.entity';
import { SavingsTransaction, TransactionType, TransactionStatus } from '../savings/entities/savings-transaction.entity';
import { Loan } from '../loans/entities/loan.entity';

export interface PostDoubleEntryDto {
  transactionReferenceId: string;
  memberId: string;
  amountSantim: string;
  debitAccount: LedgerAccountType;
  creditAccount: LedgerAccountType;
  description: string;
  isLoanRepayment?: boolean;
  targetLoanId?: string;
  paymentProvider?: string;
}

@Injectable()
export class LedgerService {
  constructor(private readonly dataSource: DataSource) {}

  /**
   * Posts an atomic, balanced double-entry transaction.
   * Debits must equal Credits. Modifies account balances within the same database transaction.
   */
  async postDoubleEntry(dto: PostDoubleEntryDto): Promise<void> {
    const amount = BigInt(dto.amountSantim);
    if (amount <= 0n) {
      throw new BadRequestException('Transaction amount must be greater than zero santim.');
    }

    await this.dataSource.transaction(async (manager) => {
      // 1. Create DEBIT entry
      const debitEntry = manager.create(LedgerEntry, {
        transactionReferenceId: dto.transactionReferenceId,
        memberId: dto.memberId,
        accountType: dto.debitAccount,
        entryType: LedgerEntryType.DEBIT,
        amountSantim: dto.amountSantim,
        currency: 'ETB',
        description: dto.description,
      });

      // 2. Create balanced CREDIT entry
      const creditEntry = manager.create(LedgerEntry, {
        transactionReferenceId: dto.transactionReferenceId,
        memberId: dto.memberId,
        accountType: dto.creditAccount,
        entryType: LedgerEntryType.CREDIT,
        amountSantim: dto.amountSantim,
        currency: 'ETB',
        description: dto.description,
      });

      await manager.save(LedgerEntry, [debitEntry, creditEntry]);

      // 3. Update Domain Balances atomically
      if (dto.isLoanRepayment && dto.targetLoanId) {
        // Loan Repayment: Reduce loan remaining balance
        const loan = await manager.findOne(Loan, { where: { id: dto.targetLoanId } });
        if (loan) {
          const remaining = BigInt(loan.balanceRemainingSantim) - amount;
          loan.balanceRemainingSantim = (remaining < 0n ? 0n : remaining).toString();
          loan.paidInstallments = (loan.paidInstallments || 0) + 1;
          await manager.save(Loan, loan);
        }
      } else {
        // Savings Deposit: Credit member savings account and record transaction
        let savingsAccount = await manager.findOne(SavingsAccount, {
          where: { memberId: dto.memberId },
        });

        if (!savingsAccount) {
          savingsAccount = manager.create(SavingsAccount, {
            memberId: dto.memberId,
            accountNumber: `VBL-SAV-${Math.floor(100000 + Math.random() * 900000)}`,
            totalSavingsSantim: '0',
            availableSantim: '0',
            frozenSantim: '0',
            totalInterestEarnedSantim: '0',
            currency: 'ETB',
          });
          await manager.save(SavingsAccount, savingsAccount);
        }

        const currentTotal = BigInt(savingsAccount.totalSavingsSantim);
        const currentAvail = BigInt(savingsAccount.availableSantim);

        savingsAccount.totalSavingsSantim = (currentTotal + amount).toString();
        savingsAccount.availableSantim = (currentAvail + amount).toString();
        await manager.save(SavingsAccount, savingsAccount);

        // Record SavingsTransaction history item
        const tx = manager.create(SavingsTransaction, {
          memberId: dto.memberId,
          savingsAccountId: savingsAccount.id,
          transactionType: TransactionType.DEPOSIT,
          amountSantim: dto.amountSantim,
          balanceBeforeSantim: currentTotal.toString(),
          balanceAfterSantim: (currentTotal + amount).toString(),
          reference: dto.transactionReferenceId,
          paymentProvider: dto.paymentProvider || 'TELEBIRR',
          status: TransactionStatus.COMPLETED,
          description: dto.description,
        });
        await manager.save(SavingsTransaction, tx);
      }
    });
  }
}
