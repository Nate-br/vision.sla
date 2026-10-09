import { Injectable } from '@nestjs/common';
// TODO(Phase 1): implement AuthService
// - OTP generation (6 digits, 5 min TTL, rate-limited per phone+device)
// - Argon2id PIN hashing
// - JWT access + refresh token generation
// - Refresh token rotation with reuse detection
// - Session device binding
@Injectable()
export class AuthService {}
