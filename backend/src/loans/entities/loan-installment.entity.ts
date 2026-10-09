import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

export enum InstallmentStatus {
  PENDING = 'PENDING',
  PARTIALLY_PAID = 'PARTIALLY_PAID',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
}

@Entity('loan_installments')
export class LoanInstallment {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  loanId: string;

  @Column({ type: 'int' })
  installmentNumber: number;

  @Column({ type: 'date' })
  dueDate: string;

  // Due amounts (santim)
  @Column({ type: 'bigint' })
  principalDueSantim: string;

  @Column({ type: 'bigint' })
  interestDueSantim: string;

  @Column({ type: 'bigint' })
  totalDueSantim: string;

  // Paid amounts (santim)
  @Column({ type: 'bigint', default: '0' })
  principalPaidSantim: string;

  @Column({ type: 'bigint', default: '0' })
  interestPaidSantim: string;

  @Column({ type: 'bigint', default: '0' })
  totalPaidSantim: string;

  @Column({
    type: 'enum',
    enum: InstallmentStatus,
    default: InstallmentStatus.PENDING,
  })
  status: InstallmentStatus;

  @Column({ type: 'timestamp with time zone', nullable: true })
  paidAt: Date | null;

  @CreateDateColumn({ type: 'timestamp with time zone' })
  createdAt: Date;

  @UpdateDateColumn({ type: 'timestamp with time zone' })
  updatedAt: Date;
}
