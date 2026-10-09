import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { SavingsAccount } from './entities/savings-account.entity';
import { SavingsTransaction, TransactionType, TransactionStatus } from './entities/savings-transaction.entity';

@Injectable()
export class SavingsService {
  constructor(
    @InjectRepository(SavingsAccount)
    private readonly accountRepo: Repository<SavingsAccount>,
    @InjectRepository(SavingsTransaction)
    private readonly transactionRepo: Repository<SavingsTransaction>,
  ) {}

  /**
   * Returns current savings balance and status. Auto-initializes on first view.
   */
  async getSummary(memberId: string) {
    let account = await this.accountRepo.findOne({ where: { memberId } });
    if (!account) {
      // Auto-initialize standard member savings account
      const accountSuffix = Math.floor(100000 + Math.random() * 900000);
      account = this.accountRepo.create({
        memberId,
        accountNumber: `VBL-SAV-${accountSuffix}`,
        totalSavingsSantim: '0',
        availableSantim: '0',
        frozenSantim: '0',
        totalInterestEarnedSantim: '0',
        currency: 'ETB',
        isActive: true,
      });
      await this.accountRepo.save(account);
    }

    return {
      accountNumber: account.accountNumber,
      totalSavingsSantim: account.totalSavingsSantim.toString(),
      availableSantim: account.availableSantim.toString(),
      frozenSantim: account.frozenSantim.toString(),
      totalInterestEarnedSantim: account.totalInterestEarnedSantim.toString(),
      currency: account.currency,
      isActive: account.isActive,
    };
  }

  /**
   * Retrieves paginated transaction history for the member.
   */
  async getTransactions(memberId: string, page: number = 1, limit: number = 20) {
    const skip = (page - 1) * limit;
    const [items, total] = await this.transactionRepo.findAndCount({
      where: { memberId },
      order: { createdAt: 'DESC' },
      skip,
      take: limit,
    });

    return {
      data: items.map((tx) => ({
        id: tx.id,
        transactionType: tx.transactionType,
        amountSantim: tx.amountSantim.toString(),
        balanceBeforeSantim: tx.balanceBeforeSantim.toString(),
        balanceAfterSantim: tx.balanceAfterSantim.toString(),
        reference: tx.reference,
        paymentProvider: tx.paymentProvider,
        status: tx.status,
        description: tx.description,
        createdAt: tx.createdAt,
      })),
      total,
      page,
      limit,
      totalPages: Math.ceil(total / limit),
    };
  }
}
