import { OtpService, OTP_TTL_MS, OTP_COOLDOWN_MS } from './otp.service';
import { OtpChallenge } from './entities/otp-challenge.entity';
import { Repository } from 'typeorm';
import { HttpException, BadRequestException } from '@nestjs/common';

describe('OtpService', () => {
  let otpService: OtpService;
  let otpRepo: Partial<Repository<OtpChallenge>>;

  beforeEach(() => {
    otpRepo = {
      findOne: jest.fn(),
      count: jest.fn().mockResolvedValue(0),
      create: jest.fn().mockImplementation((dto) => ({ id: 'ch-1', ...dto })),
      save: jest.fn().mockImplementation((ch) => Promise.resolve(ch)),
    };

    otpService = new OtpService(otpRepo as Repository<OtpChallenge>);
  });

  it('should generate a 6-digit OTP code', () => {
    const code = otpService.generateOtpCode();
    expect(code).toMatch(/^\d{6}$/);
  });

  it('should create an OTP challenge when cooldown and rate limits are respected', async () => {
    (otpRepo.findOne as jest.Mock).mockResolvedValue(null);

    const challenge = await otpService.createChallenge('+251911223344', 'fp-123');
    expect(challenge.otp).toMatch(/^\d{6}$/);
    expect(challenge.cooldownSeconds).toBe(60);
    expect(otpRepo.create).toHaveBeenCalled();
    expect(otpRepo.save).toHaveBeenCalled();
  });

  it('should reject challenge request during 60-second cooldown', async () => {
    const recentChallenge: OtpChallenge = {
      id: 'ch-recent',
      phoneNumber: '+251911223344',
      deviceFingerprint: 'fp-123',
      otpHash: 'hash',
      attempts: 0,
      isUsed: false,
      expiresAt: new Date(Date.now() + OTP_TTL_MS),
      createdAt: new Date(Date.now() - 30 * 1000), // 30 seconds ago
    };

    (otpRepo.findOne as jest.Mock).mockResolvedValue(recentChallenge);

    await expect(
      otpService.createChallenge('+251911223344', 'fp-123'),
    ).rejects.toThrow(HttpException);
  });

  it('should verify valid OTP successfully', async () => {
    const rawOtp = '123456';
    const phone = '+251911223344';
    const otpHash = otpService.hashOtp(rawOtp, phone);

    const activeChallenge: OtpChallenge = {
      id: 'ch-1',
      phoneNumber: phone,
      deviceFingerprint: 'fp-123',
      otpHash,
      attempts: 0,
      isUsed: false,
      expiresAt: new Date(Date.now() + OTP_TTL_MS),
      createdAt: new Date(),
    };

    (otpRepo.findOne as jest.Mock).mockResolvedValue(activeChallenge);

    const result = await otpService.verifyOtp(phone, rawOtp, 'fp-123');
    expect(result).toBe(true);
    expect(activeChallenge.isUsed).toBe(true);
  });

  it('should reject wrong OTP and track attempts', async () => {
    const phone = '+251911223344';
    const otpHash = otpService.hashOtp('123456', phone);

    const activeChallenge: OtpChallenge = {
      id: 'ch-1',
      phoneNumber: phone,
      deviceFingerprint: 'fp-123',
      otpHash,
      attempts: 0,
      isUsed: false,
      expiresAt: new Date(Date.now() + OTP_TTL_MS),
      createdAt: new Date(),
    };

    (otpRepo.findOne as jest.Mock).mockResolvedValue(activeChallenge);

    await expect(
      otpService.verifyOtp(phone, '999999', 'fp-123'),
    ).rejects.toThrow(BadRequestException);

    expect(activeChallenge.attempts).toBe(1);
  });
});
