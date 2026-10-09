import { MembersService } from './members.service';
import { Member, UserRole, KycStatus } from '../auth/entities/member.entity';
import { CustomerProfile } from './entities/customer-profile.entity';
import { KycDocument, KycDocumentType } from './entities/kyc-document.entity';
import { Repository } from 'typeorm';
import { BadRequestException } from '@nestjs/common';

describe('MembersService', () => {
  let service: MembersService;
  let memberRepo: Partial<Repository<Member>>;
  let profileRepo: Partial<Repository<CustomerProfile>>;
  let kycDocRepo: Partial<Repository<KycDocument>>;

  beforeEach(() => {
    memberRepo = {
      findOne: jest.fn(),
      update: jest.fn().mockResolvedValue({ affected: 1 }),
    };

    profileRepo = {
      findOne: jest.fn(),
      create: jest.fn().mockImplementation((dto) => ({ id: 'prof-1', ...dto })),
      save: jest.fn().mockImplementation((p) => Promise.resolve(p)),
    };

    kycDocRepo = {
      find: jest.fn().mockResolvedValue([]),
      findOne: jest.fn(),
      create: jest.fn().mockImplementation((d) => ({ id: 'doc-1', ...d })),
      save: jest.fn().mockImplementation((d) => Promise.resolve(d)),
    };

    service = new MembersService(
      memberRepo as Repository<Member>,
      profileRepo as Repository<CustomerProfile>,
      kycDocRepo as Repository<KycDocument>,
    );
  });

  it('should return member profile with kyc document status', async () => {
    const mockMember: Member = {
      id: 'mem-123',
      phoneNumber: '+251911223344',
      pinHash: 'hash',
      failedPinAttempts: 0,
      pinLockoutUntil: null,
      role: UserRole.MEMBER,
      kycStatus: KycStatus.NOT_SUBMITTED,
      fullName: 'Abebe Kebede',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    (memberRepo.findOne as jest.Mock).mockResolvedValue(mockMember);
    (profileRepo.findOne as jest.Mock).mockResolvedValue(null);

    const result = await service.getProfile('mem-123');
    expect(result.memberId).toBe('mem-123');
    expect(result.kycStatus).toBe(KycStatus.NOT_SUBMITTED);
  });

  it('should reject unsupported KYC file format', async () => {
    const invalidBuffer = Buffer.from('fake-pdf-content');

    await expect(
      service.processAndStoreKycDocument(
        'mem-123',
        KycDocumentType.NATIONAL_ID_FRONT,
        'doc.pdf',
        invalidBuffer,
        'application/pdf',
      ),
    ).rejects.toThrow(BadRequestException);
  });
});
