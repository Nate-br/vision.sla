import { TokenService } from './token.service';
import { JwtService } from '@nestjs/jwt';
import { Session } from './entities/session.entity';
import { Member, UserRole, KycStatus } from './entities/member.entity';
import { Repository } from 'typeorm';
import { UnauthorizedException } from '@nestjs/common';

describe('TokenService', () => {
  let tokenService: TokenService;
  let jwtService: JwtService;
  let sessionRepo: Partial<Repository<Session>>;

  const mockMember: Member = {
    id: 'mem-123',
    phoneNumber: '+251911223344',
    pinHash: null,
    failedPinAttempts: 0,
    pinLockoutUntil: null,
    role: UserRole.MEMBER,
    kycStatus: KycStatus.NOT_SUBMITTED,
    fullName: null,
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  };

  beforeEach(() => {
    jwtService = {
      sign: jest.fn().mockReturnValue('mock_access_token'),
    } as any;

    sessionRepo = {
      create: jest.fn().mockImplementation((dto) => ({ id: 'sess-1', ...dto })),
      save: jest.fn().mockImplementation((sess) => Promise.resolve(sess)),
      findOne: jest.fn(),
      update: jest.fn().mockResolvedValue({ affected: 1 }),
    };

    tokenService = new TokenService(jwtService, sessionRepo as Repository<Session>);
  });

  it('should issue new access and refresh tokens', async () => {
    const tokens = await tokenService.issueTokens(mockMember, 'dev-123');

    expect(tokens.accessToken).toBe('mock_access_token');
    expect(tokens.refreshToken).toBeDefined();
    expect(tokens.expiresIn).toBe(900);
    expect(sessionRepo.create).toHaveBeenCalled();
    expect(sessionRepo.save).toHaveBeenCalled();
  });

  it('should rotate valid refresh token', async () => {
    const rawToken = 'valid_raw_token';
    const tokenHash = tokenService.hashToken(rawToken);

    const activeSession: Session = {
      id: 'sess-1',
      memberId: 'mem-123',
      deviceId: 'dev-123',
      refreshTokenHash: tokenHash,
      familyId: 'family-1',
      isRevoked: false,
      expiresAt: new Date(Date.now() + 86400000),
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    (sessionRepo.findOne as jest.Mock).mockResolvedValue(activeSession);

    const newTokens = await tokenService.rotateTokens(rawToken, mockMember, 'dev-123');
    expect(newTokens.accessToken).toBe('mock_access_token');
    expect(activeSession.isRevoked).toBe(true);
    expect(sessionRepo.save).toHaveBeenCalledWith(activeSession);
  });

  it('should detect reuse and revoke entire family if revoked token is submitted', async () => {
    const rawToken = 'reused_token';
    const tokenHash = tokenService.hashToken(rawToken);

    const revokedSession: Session = {
      id: 'sess-1',
      memberId: 'mem-123',
      deviceId: 'dev-123',
      refreshTokenHash: tokenHash,
      familyId: 'family-compromised',
      isRevoked: true, // ALREADY REVOKED!
      expiresAt: new Date(Date.now() + 86400000),
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    (sessionRepo.findOne as jest.Mock).mockResolvedValue(revokedSession);

    await expect(
      tokenService.rotateTokens(rawToken, mockMember, 'dev-123'),
    ).rejects.toThrow(UnauthorizedException);

    // Verify all sessions in this family were revoked
    expect(sessionRepo.update).toHaveBeenCalledWith(
      { familyId: 'family-compromised' },
      { isRevoked: true },
    );
  });
});
