import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  Index,
} from 'typeorm';

@Entity('webhook_events')
export class WebhookEvent {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index({ unique: true })
  @Column({ type: 'varchar', length: 150 })
  eventId: string; // From X-Webhook-Event-Id header (prevents replay/duplicates)

  @Column({ type: 'varchar', length: 50 })
  eventType: string; // e.g. verification.completed

  @Column({ type: 'jsonb' })
  payload: Record<string, any>;

  @Column({ type: 'boolean', default: false })
  isProcessed: boolean;

  @Column({ type: 'timestamp with time zone', nullable: true })
  processedAt: Date | null;

  @Column({ type: 'text', nullable: true })
  processingError: string | null;

  @CreateDateColumn({ type: 'timestamp with time zone' })
  receivedAt: Date;
}
