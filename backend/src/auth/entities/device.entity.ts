import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

@Entity('devices')
export class Device {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Index()
  @Column({ type: 'varchar', length: 128 })
  deviceFingerprint: string;

  @Column({ type: 'varchar', length: 100, nullable: true })
  deviceModel: string | null;

  @Column({ type: 'varchar', length: 50, nullable: true })
  osVersion: string | null;

  @Column({ type: 'varchar', length: 45, nullable: true })
  lastIpAddress: string | null;

  @Column({ type: 'boolean', default: false })
  isTrusted: boolean;

  @CreateDateColumn()
  firstSeenAt: Date;

  @UpdateDateColumn()
  lastActiveAt: Date;
}
