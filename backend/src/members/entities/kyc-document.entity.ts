import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';

export enum KycDocumentType {
  NATIONAL_ID_FRONT = 'NATIONAL_ID_FRONT',
  NATIONAL_ID_BACK = 'NATIONAL_ID_BACK',
  PASSPORT = 'PASSPORT',
  KEBELE_ID = 'KEBELE_ID',
  SELFIE = 'SELFIE',
}

export enum KycDocumentStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

@Entity('kyc_documents')
export class KycDocument {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Index()
  @Column({ type: 'uuid' })
  memberId: string;

  @Column({ type: 'enum', enum: KycDocumentType })
  documentType: KycDocumentType;

  // Stored outside public webroot (e.g. /var/vbl/kyc_storage/{fileKey})
  @Column({ type: 'varchar', length: 255 })
  fileKey: string;

  @Column({ type: 'varchar', length: 255 })
  originalFilename: string;

  @Column({ type: 'varchar', length: 50 })
  mimeType: string;

  @Column({ type: 'int' })
  fileSizeBytes: number;

  @Column({
    type: 'enum',
    enum: KycDocumentStatus,
    default: KycDocumentStatus.PENDING,
  })
  status: KycDocumentStatus;

  @Column({ type: 'text', nullable: true })
  rejectionReason: string | null;

  @Column({ type: 'uuid', nullable: true })
  reviewedBy: string | null;

  @Column({ type: 'timestamp with time zone', nullable: true })
  reviewedAt: Date | null;

  @CreateDateColumn({ type: 'timestamp with time zone' })
  createdAt: Date;

  @UpdateDateColumn({ type: 'timestamp with time zone' })
  updatedAt: Date;
}
