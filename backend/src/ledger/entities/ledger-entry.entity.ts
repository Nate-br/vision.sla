import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  Index,
} from 'typeorm';

export enum LedgerEntryType {
  DEBIT = 'DEBIT',
  CREDIT = 'CREDIT',
}

export enum LedgerAccountType {
  ASSET_BANK_CLEARING = 'ASSET_BANK_CLEARING',       // e.g. Telebirr/CBE clearing account
  LIABILITY_MEMBER_SAVINGS = 'LIABILITY_MEMBER_SAVINGS', // Member's savings deposits
  ASSET_LOAN_RECEIVABLE = 'ASSET_LOAN_RECEIVABLE',     // Outstanding loan balance
  REVENUE_FEES = 'REVENUE_FEES',                       // Processing or membership fees
  REVENUE_INTEREST = 'REVENUE_INTEREST',               // Loan interest earned
}

@Entity('ledger_entries')
export class LedgerEntry {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  transactionReferenceId: string; // References PaymentAttempt.id or loan schedule item

  @Column({ type: 'enum', enum: LedgerAccountType })
  accountType: LedgerAccountType;

  @Column({ type: 'enum', enum: LedgerEntryType })
  entryType: LedgerEntryType;

  // Amount in minor units (santim)
  @Column({ type: 'bigint' })
  amountSantim: string;

  @Column({ type: 'varchar', length: 3, default: 'ETB' })
  currency: string;

  @Column({ type: 'uuid', nullable: true })
  memberId: string | null;

  @Column({ type: 'text', nullable: true })
  description: string | null;

  // Append-only constraint: records are never updated or deleted
  @CreateDateColumn()
  createdAt: Date;
}
