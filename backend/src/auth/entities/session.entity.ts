import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

@Entity('sessions')
export class Session {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Index()
  @Column({ type: 'uuid' })
  deviceId: string;

  @Column({ type: 'varchar', length: 255 })
  refreshTokenHash: string;

  @Index()
  @Column({ type: 'uuid' })
  familyId: string;

  @Column({ type: 'boolean', default: false })
  isRevoked: boolean;

  @Column({ type: 'timestamp' })
  expiresAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
