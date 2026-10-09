import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as sharp from 'sharp';
import * as fs from 'fs';
import * as path from 'path';
import { v4 as uuidv4 } from 'uuid';

import { CustomerProfile } from './entities/customer-profile.entity';
import { KycDocument, KycDocumentType, KycDocumentStatus } from './entities/kyc-document.entity';
import { Member, KycStatus } from '../auth/entities/member.entity';
import { UpdateProfileDto } from './dto/update-profile.dto';

const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const KYC_STORAGE_DIR = process.env.KYC_STORAGE_PATH || path.join(process.cwd(), 'storage', 'kyc');

@Injectable()
export class MembersService {
  constructor(
    @InjectRepository(Member)
    private readonly memberRepo: Repository<Member>,
    @InjectRepository(CustomerProfile)
    private readonly profileRepo: Repository<CustomerProfile>,
    @InjectRepository(KycDocument)
    private readonly kycDocRepo: Repository<KycDocument>,
  ) {
    if (!fs.existsSync(KYC_STORAGE_DIR)) {
      fs.mkdirSync(KYC_STORAGE_DIR, { recursive: true });
    }
  }

  async getProfile(memberId: string) {
    const member = await this.memberRepo.findOne({ where: { id: memberId } });
    if (!member) {
      throw new NotFoundException('Member not found');
    }

    let profile = await this.profileRepo.findOne({ where: { memberId } });
    if (!profile) {
      profile = this.profileRepo.create({ memberId });
      await this.profileRepo.save(profile);
    }

    const documents = await this.kycDocRepo.find({
      where: { memberId },
      order: { createdAt: 'DESC' },
    });

    return {
      memberId: member.id,
      phoneNumber: member.phoneNumber,
      role: member.role,
      kycStatus: member.kycStatus,
      profile,
      kycDocuments: documents.map((doc) => ({
        id: doc.id,
        documentType: doc.documentType,
        status: doc.status,
        rejectionReason: doc.rejectionReason,
        createdAt: doc.createdAt,
      })),
    };
  }

  async updateProfile(memberId: string, dto: UpdateProfileDto) {
    let profile = await this.profileRepo.findOne({ where: { memberId } });
    if (!profile) {
      profile = this.profileRepo.create({ memberId, ...dto });
    } else {
      Object.assign(profile, dto);
    }
    await this.profileRepo.save(profile);

    // If national ID is set on profile, reflect in member record
    if (dto.fullName) {
      await this.memberRepo.update({ id: memberId }, { fullName: dto.fullName });
    }

    return profile;
  }

  /**
   * Sanitizes uploaded KYC image using Sharp:
   * 1. Strips EXIF metadata (GPS, camera info).
   * 2. Auto-orients the image.
   * 3. Bounds size within 1920x1920.
   * 4. Re-encodes to WebP to eliminate file execution risks.
   */
  async processAndStoreKycDocument(
    memberId: string,
    documentType: KycDocumentType,
    originalFilename: string,
    buffer: Buffer,
    mimeType: string,
  ) {
    if (!ALLOWED_MIME_TYPES.includes(mimeType)) {
      throw new BadRequestException(
        `Invalid file format. Only JPEG, PNG, and WebP images are allowed. Received: ${mimeType}`,
      );
    }

    let sanitizedBuffer: Buffer;
    try {
      sanitizedBuffer = await sharp(buffer)
        .rotate()
        .resize({ width: 1920, height: 1920, fit: 'inside', withoutEnlargement: true })
        .webp({ quality: 85 })
        .toBuffer();
    } catch (err: any) {
      throw new BadRequestException('Image processing failed: Malformed or unreadable file.');
    }

    const fileKey = `${memberId}_${documentType}_${uuidv4()}.webp`;
    const destinationPath = path.join(KYC_STORAGE_DIR, fileKey);

    await fs.promises.writeFile(destinationPath, sanitizedBuffer);

    let doc = await this.kycDocRepo.findOne({ where: { memberId, documentType } });
    if (!doc) {
      doc = this.kycDocRepo.create({
        memberId,
        documentType,
        fileKey,
        originalFilename,
        mimeType: 'image/webp',
        fileSizeBytes: sanitizedBuffer.length,
        status: KycDocumentStatus.PENDING,
      });
    } else {
      doc.fileKey = fileKey;
      doc.originalFilename = originalFilename;
      doc.mimeType = 'image/webp';
      doc.fileSizeBytes = sanitizedBuffer.length;
      doc.status = KycDocumentStatus.PENDING;
      doc.rejectionReason = null;
    }
    await this.kycDocRepo.save(doc);

    // Update member's top-level KYC status to PENDING
    await this.memberRepo.update({ id: memberId }, { kycStatus: KycStatus.PENDING });

    return {
      id: doc.id,
      documentType: doc.documentType,
      status: doc.status,
      fileSizeBytes: doc.fileSizeBytes,
      message: 'Document uploaded and sanitized successfully.',
    };
  }
}
