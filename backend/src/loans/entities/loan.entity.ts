import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

export enum LoanType {
  BUSINESS = 'BUSINESS',
  PERSONAL = 'PERSONAL',
  EMERGENCY = 'EMERGENCY',
  SALARY_ADVANCE = 'SALARY_ADVANCE',
}

export enum LoanStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  ACTIVE = 'ACTIVE',
  PAID_OFF = 'PAID_OFF',
  DEFAULTED = 'DEFAULTED',
  REJECTED = 'REJECTED',
}

@Entity('loans')
export class Loan {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Column({ type: 'enum', enum: LoanType, default: LoanType.PERSONAL })
  loanType: LoanType;

  @Column({ type: 'varchar', length: 100 })
  productName: string;

  // Principal amount in minor units (santim)
  @Column({ type: 'bigint' })
  principalSantim: string;

  // Interest rate in basis points (100 bps = 1.00%, 1000 bps = 10.00%)
  @Column({ type: 'int' })
  interestRateBps: number;

  @Column({ type: 'bigint' })
  interestAmountSantim: string;

  @Column({ type: 'bigint' })
  totalDueSantim: string;

  @Column({ type: 'bigint' })
  balanceRemainingSantim: string;

  @Column({
    type: 'enum',
    enum: LoanStatus,
    default: LoanStatus.PENDING,
  })
  status: LoanStatus;

  @Column({ type: 'int' })
  termMonths: number;

  @Column({ type: 'bigint' })
  monthlyInstallmentSantim: string;

  @Column({ type: 'date', nullable: true })
  nextPaymentDueDate: string | null;

  @Column({ type: 'int', default: 0 })
  totalInstallments: number;

  @Column({ type: 'int', default: 0 })
  paidInstallments: number;

  @Column({ type: 'timestamp', nullable: true })
  disbursedAt: Date | null;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
