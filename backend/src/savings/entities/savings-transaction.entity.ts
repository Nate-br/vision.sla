import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  Index,
} from 'typeorm';

export enum TransactionType {
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  INTEREST_CREDIT = 'INTEREST_CREDIT',
  FEE = 'FEE',
  LOAN_REPAYMENT = 'LOAN_REPAYMENT',
}

export enum TransactionStatus {
  COMPLETED = 'COMPLETED',
  PENDING = 'PENDING',
  FAILED = 'FAILED',
}

@Entity('savings_transactions')
export class SavingsTransaction {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Index()
  @Column({ type: 'uuid' })
  savingsAccountId: string;

  @Column({ type: 'enum', enum: TransactionType })
  transactionType: TransactionType;

  // Amount in minor units (santim)
  @Column({ type: 'bigint' })
  amountSantim: string;

  @Column({ type: 'bigint' })
  balanceBeforeSantim: string;

  @Column({ type: 'bigint' })
  balanceAfterSantim: string;

  @Column({ type: 'varchar', length: 100, nullable: true })
  reference: string | null;

  @Column({ type: 'varchar', length: 50, nullable: true })
  paymentProvider: string | null; // e.g. TELEBIRR, CBE

  @Column({
    type: 'enum',
    enum: TransactionStatus,
    default: TransactionStatus.COMPLETED,
  })
  status: TransactionStatus;

  @Column({ type: 'text', nullable: true })
  description: string | null;

  @CreateDateColumn()
  createdAt: Date;
}
