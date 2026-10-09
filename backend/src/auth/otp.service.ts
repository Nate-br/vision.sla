import { Injectable, BadRequestException, HttpException, HttpStatus } from '@nestjs/common';
import * as crypto from 'crypto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, MoreThan } from 'typeorm';
import { OtpChallenge } from './entities/otp-challenge.entity';

export const OTP_TTL_MS = 5 * 60 * 1000; // 5 minutes
export const OTP_COOLDOWN_MS = 60 * 1000; // 60 seconds cooldown
export const MAX_VERIFY_ATTEMPTS = 3;

@Injectable()
export class OtpService {
  constructor(
    @InjectRepository(OtpChallenge)
    private readonly otpRepo: Repository<OtpChallenge>,
  ) {}

  /**
   * Generates a 6-digit cryptographic OTP.
   */
  generateOtpCode(): string {
    return crypto.randomInt(100000, 1000000).toString();
  }

  /**
   * Hashes the OTP using SHA-256 with phone number salt.
   */
  hashOtp(otp: string, phoneNumber: string): string {
    return crypto.createHmac('sha256', phoneNumber).update(otp).digest('hex');
  }

  /**
   * Creates a new OTP challenge, enforcing cooldown and rate limits.
   */
  async createChallenge(phoneNumber: string, deviceFingerprint: string): Promise<{ otp: string; expiresAt: Date; cooldownSeconds: number }> {
    const now = new Date();

    // 1. Enforce 60s cooldown per phone number
    const recentChallenge = await this.otpRepo.findOne({
      where: { phoneNumber, isUsed: false },
      order: { createdAt: 'DESC' },
    });

    if (recentChallenge) {
      const elapsedMs = now.getTime() - recentChallenge.createdAt.getTime();
      if (elapsedMs < OTP_COOLDOWN_MS) {
        const remainingCooldown = Math.ceil((OTP_COOLDOWN_MS - elapsedMs) / 1000);
        throw new HttpException(
          {
            statusCode: HttpStatus.TOO_MANY_REQUESTS,
            error: 'Too Many Requests',
            message: `Please wait ${remainingCooldown} seconds before requesting a new OTP.`,
            cooldownSeconds: remainingCooldown,
          },
          HttpStatus.TOO_MANY_REQUESTS,
        );
      }
    }

    // 2. Enforce rate limit (max 5 OTP requests per hour per phone)
    const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);
    const hourlyCount = await this.otpRepo.count({
      where: { phoneNumber, createdAt: MoreThan(oneHourAgo) },
    });

    if (hourlyCount >= 5) {
      throw new HttpException(
        {
          statusCode: HttpStatus.TOO_MANY_REQUESTS,
          error: 'Rate Limit Exceeded',
          message: 'Maximum OTP request limit reached for this phone number. Try again in an hour.',
        },
        HttpStatus.TOO_MANY_REQUESTS,
      );
    }

    const rawOtp = this.generateOtpCode();
    const otpHash = this.hashOtp(rawOtp, phoneNumber);
    const expiresAt = new Date(now.getTime() + OTP_TTL_MS);

    const challenge = this.otpRepo.create({
      phoneNumber,
      deviceFingerprint,
      otpHash,
      attempts: 0,
      isUsed: false,
      expiresAt,
    });

    await this.otpRepo.save(challenge);

    return {
      otp: rawOtp,
      expiresAt,
      cooldownSeconds: 60,
    };
  }

  /**
   * Verifies an OTP code against active challenges for the phone and device.
   */
  async verifyOtp(phoneNumber: string, otp: string, deviceFingerprint: string): Promise<boolean> {
    const now = new Date();

    const challenge = await this.otpRepo.findOne({
      where: {
        phoneNumber,
        isUsed: false,
      },
      order: { createdAt: 'DESC' },
    });

    if (!challenge) {
      throw new BadRequestException('No active OTP challenge found. Please request a new code.');
    }

    if (challenge.expiresAt < now) {
      throw new BadRequestException('OTP has expired. Please request a new code.');
    }

    if (challenge.attempts >= MAX_VERIFY_ATTEMPTS) {
      challenge.isUsed = true;
      await this.otpRepo.save(challenge);
      throw new BadRequestException('Too many incorrect attempts. This OTP has been invalidated.');
    }

    const expectedHash = this.hashOtp(otp, phoneNumber);
    if (challenge.otpHash !== expectedHash) {
      challenge.attempts += 1;
      await this.otpRepo.save(challenge);
      const remaining = MAX_VERIFY_ATTEMPTS - challenge.attempts;
      throw new BadRequestException(`Incorrect OTP code. ${remaining} attempt(s) remaining.`);
    }

    // Mark as used
    challenge.isUsed = true;
    await this.otpRepo.save(challenge);
    return true;
  }
}
