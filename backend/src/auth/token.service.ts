import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as crypto from 'crypto';
import { v4 as uuidv4 } from 'uuid';
import { Session } from './entities/session.entity';
import { Member } from './entities/member.entity';

export const ACCESS_TOKEN_TTL_SECONDS = 15 * 60; // 15 minutes
export const REFRESH_TOKEN_TTL_DAYS = 30; // 30 days

export interface TokenPayload {
  sub: string;
  role: string;
  deviceId: string;
  familyId: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

@Injectable()
export class TokenService {
  constructor(
    private readonly jwtService: JwtService,
    @InjectRepository(Session)
    private readonly sessionRepo: Repository<Session>,
  ) {}

  hashToken(token: string): string {
    return crypto.createHash('sha256').update(token).digest('hex');
  }

  generateRandomToken(): string {
    return crypto.randomBytes(48).toString('base64url');
  }

  /**
   * Generates a new access token (15m) and a fresh refresh token with a new or existing familyId.
   */
  async issueTokens(member: Member, deviceId: string, existingFamilyId?: string): Promise<AuthTokens> {
    const familyId = existingFamilyId || uuidv4();
    const rawRefreshToken = this.generateRandomToken();
    const refreshTokenHash = this.hashToken(rawRefreshToken);

    const payload: TokenPayload = {
      sub: member.id,
      role: member.role,
      deviceId,
      familyId,
    };

    const accessToken = this.jwtService.sign(payload, {
      expiresIn: ACCESS_TOKEN_TTL_SECONDS,
    });

    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + REFRESH_TOKEN_TTL_DAYS);

    const session = this.sessionRepo.create({
      memberId: member.id,
      deviceId,
      refreshTokenHash,
      familyId,
      isRevoked: false,
      expiresAt,
    });

    await this.sessionRepo.save(session);

    return {
      accessToken,
      refreshToken: rawRefreshToken,
      expiresIn: ACCESS_TOKEN_TTL_SECONDS,
      tokenType: 'Bearer',
    };
  }

  /**
   * Rotates a refresh token.
   * If reuse is detected (token already revoked or invalidated), ALL sessions in the family are revoked!
   */
  async rotateTokens(rawRefreshToken: string, member: Member, deviceId: string): Promise<AuthTokens> {
    const tokenHash = this.hashToken(rawRefreshToken);
    const session = await this.sessionRepo.findOne({
      where: { refreshTokenHash: tokenHash },
    });

    if (!session) {
      throw new UnauthorizedException('Invalid refresh token.');
    }

    // Reuse Detection: If this session was already revoked, a compromised token is being reused!
    if (session.isRevoked) {
      await this.sessionRepo.update(
        { familyId: session.familyId },
        { isRevoked: true },
      );
      throw new UnauthorizedException(
        'Security alert: Refresh token reuse detected. All sessions in this chain have been revoked.',
      );
    }

    const now = new Date();
    if (session.expiresAt < now) {
      session.isRevoked = true;
      await this.sessionRepo.save(session);
      throw new UnauthorizedException('Refresh token has expired.');
    }

    // Invalidate the consumed session
    session.isRevoked = true;
    await this.sessionRepo.save(session);

    // Issue new pair continuing the same familyId
    return this.issueTokens(member, deviceId, session.familyId);
  }

  /**
   * Revokes a specific session.
   */
  async revokeSession(sessionId: string, memberId: string): Promise<void> {
    await this.sessionRepo.update(
      { id: sessionId, memberId },
      { isRevoked: true },
    );
  }

  /**
   * Revokes all sessions for a member (e.g. on password/PIN reset).
   */
  async revokeAllMemberSessions(memberId: string): Promise<void> {
    await this.sessionRepo.update(
      { memberId },
      { isRevoked: true },
    );
  }
}
