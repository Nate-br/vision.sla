import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

export enum LoanApplicationStatus {
  SUBMITTED = 'SUBMITTED',
  UNDER_REVIEW = 'UNDER_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  DISBURSED = 'DISBURSED',
  CANCELLED = 'CANCELLED',
}

@Entity('loan_applications')
export class LoanApplication {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Column({ type: 'uuid' })
  productId: string;

  @Column({ type: 'bigint' })
  requestedAmountSantim: string;

  @Column({ type: 'bigint', nullable: true })
  approvedAmountSantim: string | null;

  @Column({ type: 'int' })
  termMonths: number;

  @Column({ type: 'text' })
  purpose: string;

  @Column({
    type: 'enum',
    enum: LoanApplicationStatus,
    default: LoanApplicationStatus.SUBMITTED,
  })
  status: LoanApplicationStatus;

  @Column({ type: 'text', nullable: true })
  rejectionReason: string | null;

  @Column({ type: 'uuid', nullable: true })
  reviewedBy: string | null;

  @Column({ type: 'timestamp', nullable: true })
  reviewedAt: Date | null;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
