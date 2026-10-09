import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER',
}

@Entity('customer_profiles')
export class CustomerProfile {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index({ unique: true })
  @Column({ type: 'uuid' })
  memberId: string;

  @Column({ type: 'varchar', length: 150, nullable: true })
  fullName: string | null;

  @Column({ type: 'enum', enum: Gender, nullable: true })
  gender: Gender | null;

  @Column({ type: 'date', nullable: true })
  dateOfBirth: string | null;

  @Column({ type: 'varchar', length: 50, nullable: true })
  nationalId: string | null; // National ID or Fayda number

  @Column({ type: 'text', nullable: true })
  address: string | null;

  @Column({ type: 'varchar', length: 100, nullable: true })
  occupation: string | null;

  @Column({ type: 'varchar', length: 150, nullable: true })
  employer: string | null;

  // Monthly income stored in minor units (santim)
  @Column({ type: 'bigint', nullable: true })
  monthlyIncomeSantim: string | null;

  @Column({ type: 'varchar', length: 100, nullable: true })
  emergencyContactName: string | null;

  @Column({ type: 'varchar', length: 20, nullable: true })
  emergencyContactPhone: string | null;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
