import {
  Injectable,
  NotFoundException,
  UnauthorizedException,
  BadRequestException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Member, UserRole } from './entities/member.entity';
import { Device } from './entities/device.entity';
import { Session } from './entities/session.entity';
import { OtpService } from './otp.service';
import { PinService } from './pin.service';
import { TokenService, AuthTokens } from './token.service';
import { RequestOtpDto } from './dto/request-otp.dto';
import { VerifyOtpDto } from './dto/verify-otp.dto';
import { SetupPinDto, VerifyPinDto } from './dto/pin.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';

export interface AuthResponse extends AuthTokens {
  isPinSet: boolean;
  isNewDevice: boolean;
  member: {
    id: string;
    phoneNumber: string;
    role: UserRole;
    kycStatus: string;
  };
}

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(Member)
    private readonly memberRepo: Repository<Member>,
    @InjectRepository(Device)
    private readonly deviceRepo: Repository<Device>,
    @InjectRepository(Session)
    private readonly sessionRepo: Repository<Session>,
    private readonly otpService: OtpService,
    private readonly pinService: PinService,
    private readonly tokenService: TokenService,
  ) {}

  /**
   * Request OTP for authentication / sign-up.
   */
  async requestOtp(dto: RequestOtpDto, ipAddress: string) {
    let member = await this.memberRepo.findOne({ where: { phoneNumber: dto.phoneNumber } });

    // Anti-SIM-swap / Device change check
    let isNewDevice = false;
    if (member) {
      const knownDevice = await this.deviceRepo.findOne({
        where: { memberId: member.id, deviceFingerprint: dto.deviceFingerprint },
      });
      if (!knownDevice) {
        isNewDevice = true;
        // In production: dispatch push notification to existing active sessions/devices
      }
    }

    const challenge = await this.otpService.createChallenge(dto.phoneNumber, dto.deviceFingerprint);

    return {
      message: 'OTP sent successfully',
      expiresAt: challenge.expiresAt,
      cooldownSeconds: challenge.cooldownSeconds,
      isNewDevice,
      // Debug helper in development only (stripped in production)
      ...(process.env.NODE_ENV !== 'production' && { devOtp: challenge.otp }),
    };
  }

  /**
   * Verifies the submitted OTP.
   * If new user, creates member. If PIN is already set, issues auth tokens.
   */
  async verifyOtp(dto: VerifyOtpDto, ipAddress: string) {
    const isValid = await this.otpService.verifyOtp(dto.phoneNumber, dto.otp, dto.deviceFingerprint);
    if (!isValid) {
      throw new BadRequestException('Invalid OTP');
    }

    let member = await this.memberRepo.findOne({ where: { phoneNumber: dto.phoneNumber } });
    if (!member) {
      member = this.memberRepo.create({
        phoneNumber: dto.phoneNumber,
        role: UserRole.MEMBER,
      });
      await this.memberRepo.save(member);
    }

    // Register or update device
    let device = await this.deviceRepo.findOne({
      where: { memberId: member.id, deviceFingerprint: dto.deviceFingerprint },
    });

    let isNewDevice = false;
    if (!device) {
      isNewDevice = true;
      device = this.deviceRepo.create({
        memberId: member.id,
        deviceFingerprint: dto.deviceFingerprint,
        lastIpAddress: ipAddress,
        isTrusted: !member.pinHash, // Trusted by default only if fresh account setup
      });
    } else {
      device.lastIpAddress = ipAddress;
      device.lastActiveAt = new Date();
    }
    await this.deviceRepo.save(device);

    const isPinSet = !!member.pinHash;

    // Issue tokens
    const tokens = await this.tokenService.issueTokens(member, device.id);

    return {
      ...tokens,
      isPinSet,
      isNewDevice,
      member: {
        id: member.id,
        phoneNumber: member.phoneNumber,
        role: member.role,
        kycStatus: member.kycStatus,
      },
    };
  }

  /**
   * Sets up PIN for a member.
   */
  async setupPin(memberId: string, dto: SetupPinDto) {
    const member = await this.memberRepo.findOne({ where: { id: memberId } });
    if (!member) {
      throw new NotFoundException('Member not found');
    }

    const pinHash = await this.pinService.hashPin(dto.pin);
    member.pinHash = pinHash;
    member.failedPinAttempts = 0;
    member.pinLockoutUntil = null;
    await this.memberRepo.save(member);

    return { message: 'PIN configured successfully' };
  }

  /**
   * Full login with phone number and PIN.
   */
  async verifyPin(dto: VerifyPinDto, ipAddress: string): Promise<AuthResponse> {
    const member = await this.memberRepo.findOne({ where: { phoneNumber: dto.phoneNumber } });
    if (!member) {
      throw new UnauthorizedException('Invalid phone number or PIN');
    }

    const result = await this.pinService.verifyPin(dto.pin, member);
    await this.memberRepo.save(member);

    if (!result.success) {
      throw new UnauthorizedException('Invalid phone number or PIN');
    }

    // Register or update device
    let device = await this.deviceRepo.findOne({
      where: { memberId: member.id, deviceFingerprint: dto.deviceFingerprint },
    });

    let isNewDevice = false;
    if (!device) {
      isNewDevice = true;
      device = this.deviceRepo.create({
        memberId: member.id,
        deviceFingerprint: dto.deviceFingerprint,
        lastIpAddress: ipAddress,
        isTrusted: false,
      });
    } else {
      device.lastIpAddress = ipAddress;
      device.lastActiveAt = new Date();
    }
    await this.deviceRepo.save(device);

    const tokens = await this.tokenService.issueTokens(member, device.id);

    return {
      ...tokens,
      isPinSet: true,
      isNewDevice,
      member: {
        id: member.id,
        phoneNumber: member.phoneNumber,
        role: member.role,
        kycStatus: member.kycStatus,
      },
    };
  }

  /**
   * Rotates refresh token and issues new access token.
   */
  async refreshToken(dto: RefreshTokenDto): Promise<AuthTokens> {
    const tokenHash = this.tokenService.hashToken(dto.refreshToken);
    const session = await this.sessionRepo.findOne({ where: { refreshTokenHash: tokenHash } });
    if (!session) {
      throw new UnauthorizedException('Invalid refresh token');
    }

    const member = await this.memberRepo.findOne({ where: { id: session.memberId } });
    if (!member || !member.isActive) {
      throw new UnauthorizedException('Member account inactive or not found');
    }

    return this.tokenService.rotateTokens(dto.refreshToken, member, session.deviceId);
  }

  /**
   * Revoke current session.
   */
  async logout(sessionId: string, memberId: string) {
    await this.tokenService.revokeSession(sessionId, memberId);
    return { message: 'Logged out successfully' };
  }

  /**
   * List active sessions for member.
   */
  async getActiveSessions(memberId: string) {
    return this.sessionRepo.find({
      where: { memberId, isRevoked: false },
      order: { createdAt: 'DESC' },
    });
  }
}
