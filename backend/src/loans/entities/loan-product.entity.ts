import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

@Entity('loan_products')
export class LoanProduct {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index({ unique: true })
  @Column({ type: 'varchar', length: 50 })
  code: string;

  @Column({ type: 'varchar', length: 100 })
  name: string;

  @Column({ type: 'text' })
  description: string;

  // Limits in santim minor units
  @Column({ type: 'bigint' })
  minAmountSantim: string;

  @Column({ type: 'bigint' })
  maxAmountSantim: string;

  // Interest rate in basis points (e.g. 1200 bps = 12.00% annual)
  @Column({ type: 'int' })
  interestRateBps: number;

  @Column({ type: 'int', default: 1 })
  minTenureMonths: number;

  @Column({ type: 'int', default: 24 })
  maxTenureMonths: number;

  // Processing fee in basis points (e.g. 100 bps = 1.00%)
  @Column({ type: 'int', default: 100 })
  processingFeeBps: number;

  @Column({ type: 'boolean', default: true })
  isActive: boolean;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
