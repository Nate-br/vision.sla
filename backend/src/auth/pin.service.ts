import { Injectable, UnauthorizedException, HttpException, HttpStatus } from '@nestjs/common';
import * as argon2 from 'argon2';
import { Member } from './entities/member.entity';

export const MAX_PIN_ATTEMPTS = 5;
export const LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

@Injectable()
export class PinService {
  /**
   * Hashes a numeric PIN using Argon2id with memory-hard parameters.
   */
  async hashPin(pin: string): Promise<string> {
    return argon2.hash(pin, {
      type: argon2.argon2id,
      memoryCost: 65536, // 64 MB
      timeCost: 3,
      parallelism: 1,
    });
  }

  /**
   * Verifies the PIN against the stored Argon2id hash.
   * Handles brute-force lockout checks and failed attempt counters.
   */
  async verifyPin(pin: string, member: Member): Promise<{ success: boolean; lockoutUntil?: Date }> {
    const now = new Date();

    // Check existing lockout
    if (member.pinLockoutUntil && member.pinLockoutUntil > now) {
      const remainingSeconds = Math.ceil((member.pinLockoutUntil.getTime() - now.getTime()) / 1000);
      throw new HttpException(
        {
          statusCode: HttpStatus.FORBIDDEN,
          error: 'Account Locked',
          message: `Too many failed PIN attempts. Account locked for ${remainingSeconds} seconds.`,
          retryAfterSeconds: remainingSeconds,
        },
        HttpStatus.FORBIDDEN,
      );
    }

    if (!member.pinHash) {
      throw new UnauthorizedException('PIN has not been set for this account.');
    }

    const isValid = await argon2.verify(member.pinHash, pin);

    if (!isValid) {
      member.failedPinAttempts = (member.failedPinAttempts || 0) + 1;
      if (member.failedPinAttempts >= MAX_PIN_ATTEMPTS) {
        member.pinLockoutUntil = new Date(now.getTime() + LOCKOUT_DURATION_MS);
      }
      return { success: false, lockoutUntil: member.pinLockoutUntil || undefined };
    }

    // Success: reset failed attempts
    member.failedPinAttempts = 0;
    member.pinLockoutUntil = null;
    return { success: true };
  }
}
