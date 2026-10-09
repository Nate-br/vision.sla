import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

export enum UserRole {
  MEMBER = 'MEMBER',
  TELLER = 'TELLER',
  ACCOUNTANT = 'ACCOUNTANT',
  ADMIN = 'ADMIN',
}

export enum KycStatus {
  NOT_SUBMITTED = 'NOT_SUBMITTED',
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

@Entity('members')
export class Member {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index({ unique: true })
  @Column({ type: 'varchar', length: 20 })
  phoneNumber: string;

  @Column({ type: 'varchar', length: 255, nullable: true })
  pinHash: string | null;

  @Column({ type: 'int', default: 0 })
  failedPinAttempts: number;

  @Column({ type: 'timestamp with time zone', nullable: true })
  pinLockoutUntil: Date | null;

  @Column({
    type: 'enum',
    enum: UserRole,
    default: UserRole.MEMBER,
  })
  role: UserRole;

  @Column({
    type: 'enum',
    enum: KycStatus,
    default: KycStatus.NOT_SUBMITTED,
  })
  kycStatus: KycStatus;

  @Column({ type: 'varchar', length: 100, nullable: true })
  fullName: string | null;

  @Column({ type: 'boolean', default: true })
  isActive: boolean;

  @CreateDateColumn({ type: 'timestamp with time zone' })
  createdAt: Date;

  @UpdateDateColumn({ type: 'timestamp with time zone' })
  updatedAt: Date;
}
