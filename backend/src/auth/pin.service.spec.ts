import { PinService, MAX_PIN_ATTEMPTS, LOCKOUT_DURATION_MS } from './pin.service';
import { Member, UserRole, KycStatus } from './entities/member.entity';
import { HttpException } from '@nestjs/common';

describe('PinService', () => {
  let pinService: PinService;

  beforeEach(() => {
    pinService = new PinService();
  });

  const createMember = (overrides: Partial<Member> = {}): Member => ({
    id: 'mem-123',
    phoneNumber: '+251911223344',
    pinHash: null,
    failedPinAttempts: 0,
    pinLockoutUntil: null,
    role: UserRole.MEMBER,
    kycStatus: KycStatus.NOT_SUBMITTED,
    fullName: 'Test User',
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    ...overrides,
  });

  it('should hash a PIN and verify successfully', async () => {
    const rawPin = '1234';
    const hash = await pinService.hashPin(rawPin);
    expect(hash).toBeDefined();
    expect(hash).toContain('$argon2id$');

    const member = createMember({ pinHash: hash });
    const result = await pinService.verifyPin(rawPin, member);
    expect(result.success).toBe(true);
    expect(member.failedPinAttempts).toBe(0);
    expect(member.pinLockoutUntil).toBeNull();
  });

  it('should increment failedPinAttempts on incorrect PIN', async () => {
    const rawPin = '1234';
    const hash = await pinService.hashPin(rawPin);
    const member = createMember({ pinHash: hash, failedPinAttempts: 0 });

    const result = await pinService.verifyPin('9999', member);
    expect(result.success).toBe(false);
    expect(member.failedPinAttempts).toBe(1);
    expect(member.pinLockoutUntil).toBeNull();
  });

  it('should lock out account after 5 failed attempts', async () => {
    const rawPin = '1234';
    const hash = await pinService.hashPin(rawPin);
    const member = createMember({ pinHash: hash, failedPinAttempts: 4 });

    const result = await pinService.verifyPin('9999', member);
    expect(result.success).toBe(false);
    expect(member.failedPinAttempts).toBe(MAX_PIN_ATTEMPTS);
    expect(member.pinLockoutUntil).toBeDefined();
    expect(member.pinLockoutUntil!.getTime()).toBeGreaterThan(Date.now());
  });

  it('should reject authentication immediately when locked out', async () => {
    const rawPin = '1234';
    const hash = await pinService.hashPin(rawPin);
    const lockoutTime = new Date(Date.now() + LOCKOUT_DURATION_MS);
    const member = createMember({
      pinHash: hash,
      failedPinAttempts: 5,
      pinLockoutUntil: lockoutTime,
    });

    await expect(pinService.verifyPin(rawPin, member)).rejects.toThrow(HttpException);
  });
});
