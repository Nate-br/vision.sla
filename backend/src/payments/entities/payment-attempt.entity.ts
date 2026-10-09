import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
  Unique,
} from 'typeorm';
import { PaymentProvider, PaymentStatus } from '../../common/types/domain.types';

export enum PaymentPurpose {
  SAVINGS = 'SAVINGS',
  LOAN_REPAYMENT = 'LOAN_REPAYMENT',
  MEMBERSHIP_FEE = 'MEMBERSHIP_FEE',
}

@Entity('payment_attempts')
@Unique('UQ_provider_normalized_reference', ['paymentProvider', 'normalizedReference'])
export class PaymentAttempt {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Column({ type: 'varchar', length: 50 })
  paymentProvider: PaymentProvider;

  @Column({ type: 'varchar', length: 100 })
  rawReference: string;

  @Index()
  @Column({ type: 'varchar', length: 100 })
  normalizedReference: string;

  // Expected and credited amounts in minor units (santim)
  @Column({ type: 'bigint' })
  expectedAmountSantim: string;

  @Column({ type: 'bigint', nullable: true })
  creditedAmountSantim: string | null;

  @Column({ type: 'varchar', length: 3, default: 'ETB' })
  currency: string;

  @Column({ type: 'enum', enum: PaymentPurpose, default: PaymentPurpose.SAVINGS })
  purpose: PaymentPurpose;

  @Column({ type: 'uuid', nullable: true })
  targetLoanId: string | null;

  @Column({
    type: 'enum',
    enum: PaymentStatus,
    default: PaymentStatus.SUBMITTED,
  })
  status: PaymentStatus;

  @Column({ type: 'varchar', length: 100, nullable: true })
  verifyEtRequestId: string | null;

  @Column({ type: 'varchar', length: 50, nullable: true })
  decision: string | null; // CREDIT, REJECT, MANUAL_REVIEW, RETRY

  @Column({ type: 'text', nullable: true })
  rejectionReason: string | null;

  @Column({ type: 'varchar', length: 100, nullable: true })
  settlementAccount: string | null;

  @Column({ type: 'json', nullable: true })
  rawVerificationResponse: Record<string, any> | null;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
