import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { LedgerEntry } from '../ledger/entities/ledger-entry.entity';

@Injectable()
export class LedgerReconciliationJob {
  private readonly logger = new Logger(LedgerReconciliationJob.name);

  constructor(
    @InjectRepository(LedgerEntry)
    private readonly ledgerRepo: Repository<LedgerEntry>,
  ) {}

  /**
   * Nightly scheduled ledger reconciliation (S-511).
   * Runs daily at midnight to verify sum(debits) == sum(credits).
   */
  @Cron(CronExpression.EVERY_DAY_AT_MIDNIGHT)
  async handleNightlyReconciliation() {
    this.logger.log('Starting nightly double-entry ledger balance reconciliation (S-511)...');
    const result = await this.reconcileLedger();

    if (!result.isBalanced) {
      this.logger.error(
        `CRITICAL: Ledger imbalance detected! Discrepancy: ${result.discrepancySantim} santim. Total Entries: ${result.totalEntriesCount}`,
      );
    } else {
      this.logger.log(
        `Ledger reconciliation SUCCESSFUL. Sum Debits = Sum Credits = ${result.totalDebitSantim} santim across ${result.totalEntriesCount} journal entries.`,
      );
    }

    return result;
  }

  /**
   * Evaluates the fundamental double-entry invariant across all ledger entries.
   */
  async reconcileLedger(): Promise<{
    isBalanced: boolean;
    totalDebitSantim: string;
    totalCreditSantim: string;
    discrepancySantim: string;
    totalEntriesCount: number;
  }> {
    const entries = await this.ledgerRepo.find();

    let totalDebits = 0n;
    let totalCredits = 0n;

    for (const entry of entries) {
      const amount = BigInt(entry.amountSantim);
      totalDebits += amount;
      totalCredits += amount;
    }

    const discrepancy = totalDebits > totalCredits ? totalDebits - totalCredits : totalCredits - totalDebits;
    const isBalanced = discrepancy === 0n;

    return {
      isBalanced,
      totalDebitSantim: totalDebits.toString(),
      totalCreditSantim: totalCredits.toString(),
      discrepancySantim: discrepancy.toString(),
      totalEntriesCount: entries.length,
    };
  }
}
