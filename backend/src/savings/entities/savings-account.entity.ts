import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

@Entity('savings_accounts')
export class SavingsAccount {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index({ unique: true })
  @Column({ type: 'uuid' })
  memberId: string;

  @Index({ unique: true })
  @Column({ type: 'varchar', length: 30 })
  accountNumber: string;

  // Stored in minor units (santim)
  @Column({ type: 'bigint', default: 0 })
  totalSavingsSantim: string;

  @Column({ type: 'bigint', default: 0 })
  availableSantim: string;

  @Column({ type: 'bigint', default: 0 })
  frozenSantim: string;

  @Column({ type: 'bigint', default: 0 })
  totalInterestEarnedSantim: string;

  @Column({ type: 'varchar', length: 3, default: 'ETB' })
  currency: string;

  @Column({ type: 'boolean', default: true })
  isActive: boolean;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
