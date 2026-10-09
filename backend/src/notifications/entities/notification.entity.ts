import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  Index,
} from 'typeorm';

export enum NotificationType {
  PAYMENT = 'PAYMENT',
  LOAN = 'LOAN',
  KYC = 'KYC',
  SYSTEM = 'SYSTEM',
}

@Entity('notifications')
export class Notification {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Column({ type: 'varchar', length: 150 })
  title: string;

  @Column({ type: 'text' })
  message: string;

  @Column({ type: 'enum', enum: NotificationType, default: NotificationType.SYSTEM })
  type: NotificationType;

  @Column({ type: 'boolean', default: false })
  isRead: boolean;

  @Column({ type: 'timestamp', nullable: true })
  readAt: Date | null;

  @Column({ type: 'json', nullable: true })
  metadata: Record<string, any> | null;

  @CreateDateColumn()
  createdAt: Date;
}
