import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';
import { PaymentProvider } from '../../common/types/domain.types';

@Entity('org_receiving_accounts')
export class OrgReceivingAccount {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'varchar', length: 50 })
  provider: PaymentProvider;

  @Column({ type: 'varchar', length: 100 })
  accountNumberOrPhone: string;

  @Column({ type: 'varchar', length: 150 })
  accountHolderName: string;

  @Column({ type: 'varchar', length: 150, nullable: true })
  branchName: string | null;

  @Column({ type: 'text', nullable: true })
  instructions: string | null;

  @Column({ type: 'boolean', default: true })
  isActive: boolean;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
