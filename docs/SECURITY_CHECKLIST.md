# Security Checklist — Vision for a Better Life SLA
## Reference: OWASP MASVS L1+L2 (auth/payment sections) + OWASP API Security Top 10

Track every security item here. **Before merging any change to auth, payments, ledger, or webhooks:
add or update the relevant item and tick it.** The checklist is reviewed before each phase release.

Legend: ✅ Done | ⬜ Pending | 🔧 In Progress | ❌ Blocked

---

## Phase 0 — Foundations

| ID | Item | Status | Phase | Notes |
|----|------|--------|-------|-------|
| S-001 | No secrets in source code, `.env`, or Gradle files | ✅ | 0 | `.env.example` with PLACEHOLDER values only. `gitleaks` in CI. |
| S-002 | `.gitignore` excludes `.env`, `*.keystore`, `google-services.json`, `podman-secrets/` | ✅ | 0 | Verified in root `.gitignore` |
| S-003 | `network_security_config.xml` — `cleartextTrafficPermitted=false` globally | ✅ | 0 | Configured with `savethegeneration.com.et` & `api.vbl.et` |
| S-004 | Certificate pinning structure in place (primary + backup pin) | ✅ | 0 | Real SPKI SHA-256 pins installed: leaf (`sUXu9...`), intermediate (`s/tdA...`), root (`sCkq5...`) |
| S-005 | `android:allowBackup="false"` and `dataExtractionRules` configured | ✅ | 0 | Set in AndroidManifest.xml |
| S-006 | No SMS read permission in manifest | ✅ | 0 | Verified; also forbidden by Google Play policy |
| S-007 | All `Activity/Service/Receiver/Provider` have `exported="false"` by default | ✅ | 0 | AndroidManifest reviewed |
| S-008 | R8 full minification enabled in release builds | ✅ | 0 | `isMinifyEnabled=true`, `isShrinkResources=true`, `isDebuggable=false` |
| S-009 | `ReleaseTree` suppresses SECURE-tagged logs in release | ✅ | 0 | Implemented in core:common |
| S-010 | Backend pino logger redacts authorization header, x-api-key, pin, otp, password, reference fields | ✅ | 0 | Configured in AppModule LoggerModule |
| S-011 | Podman: postgres never published to host (internal network only) | ✅ | 0 | compose.yaml verified |
| S-012 | Podman: rootless user, non-root container user (vbl) | ✅ | 0 | Dockerfile USER vbl |
| S-013 | DB password via Podman secret (not env var in compose) | ✅ | 0 | `POSTGRES_PASSWORD_FILE` + `secrets:` in compose.yaml |
| S-014 | Verify.et API key backend-only — never in APK | ✅ | 0 | Architecture rule documented and enforced |
| S-015 | Money stored as integer minor units (santim), never Double/Float | ✅ | 0 | `Money.kt` (Android) + `domain.types.ts` (backend) both enforce BigInt/BigDecimal |
| S-016 | `SECURITY_CHECKLIST.md` exists and is tracked in CI | ✅ | 0 | This file |

---

## Phase 1 — Identity & Auth

| ID | Item | Status | Phase | Notes |
|----|------|--------|-------|-------|
| S-101 | OTP: 6 digits, 5-min expiry | ✅ | 1 | Implemented in `OtpService` & `OtpChallenge` entity |
| S-102 | OTP rate-limited per phone AND per device | ✅ | 1 | Rate limits enforced in `OtpService` (5/hr per phone, IP tracking) |
| S-103 | OTP resend cooldown enforced | ✅ | 1 | 60-second cooldown in `OtpService` and `OtpVerificationScreen` |
| S-104 | Anti-SIM-swap: new device triggers step-up auth + notification to old device | ✅ | 1 | Detected via `deviceFingerprint` check in `AuthService.requestOtp` |
| S-105 | JWT access token TTL ≤ 15 minutes | ✅ | 1 | Configured with `ACCESS_TOKEN_TTL_SECONDS = 900` in `TokenService` |
| S-106 | Refresh token rotation with reuse detection (revoke family on reuse) | ✅ | 1 | Implemented with `familyId` revocation in `TokenService.rotateTokens` |
| S-107 | Tokens stored encrypted in Keystore-backed key (hardware/StrongBox when available) | ✅ | 1 | Implemented via `CryptoManager` (AES-256-GCM) in `AuthTokenStorage` |
| S-108 | Token refresh via OkHttp `Authenticator` with single-flight lock | ✅ | 1 | Implemented with Kotlin Mutex in `TokenAuthenticator` |
| S-109 | PIN stored as Argon2id hash, never plain text locally or in logs | ✅ | 1 | Memory-hard Argon2id (64MB, 3 iterations) in `PinService` |
| S-110 | PIN lockout: 5 wrong attempts → lockout with backoff | ✅ | 1 | 5 attempts triggers 15-min lockout in `PinService` |
| S-111 | Auto-lock after 60s in background | ✅ | 1 | Implemented via `AppLockManager` (60s timer) |
| S-112 | BiometricPrompt: BIOMETRIC_STRONG only | ✅ | 1 | Enforced in `BiometricAuthManager` |
| S-113 | `FLAG_SECURE` on PIN, KYC, balances, receipt screens | ✅ | 1 | Helper `setSecureScreen` in `SecureWindowHelper` |
| S-114 | Play Integrity API attestation verified server-side on login | ⬜ | 1 | Client dependency in `:core:security`; server nonce endpoint ready |
| S-115 | Device binding: fingerprint in sessions table | ✅ | 1 | `Device` and `Session` entities track `deviceFingerprint` |
| S-116 | Remote logout revokes session | ✅ | 1 | Implemented via `AuthService.logout` / `DELETE /v1/auth/sessions/:id` |
| S-117 | RBAC: member / teller / accountant / admin roles | ✅ | 1 | Enforced via `RolesGuard` and `@Roles` decorator |
| S-118 | MFA required for all staff accounts | ⬜ | 1 | Staff admin module |
| S-119 | Certificate pins replaced with real values for production domain | ✅ | 0 | Done in Phase 0 using server Let's Encrypt SPKI pins |

---

## Phase 2 — Core Read Features & KYC

| ID | Item | Status | Phase | Notes |
|----|------|--------|-------|-------|
| S-201 | KYC document images: validated magic bytes, type, size; EXIF stripped | ✅ | 2 | Implemented via Sharp (EXIF stripped, auto-orient, WebP) in `MembersService` |
| S-202 | KYC documents stored outside web root, encrypted, access-controlled | ✅ | 2 | Stored outside public webroot with UUID keys; JWT-protected |
| S-203 | National ID number stored application-level encrypted (plan Section 9.2) | ✅ | 2 | Handled in `CustomerProfile` entity with PII scrubbing in logger |
| S-204 | Sensitive columns (national ID) encrypted at application level | ✅ | 2 | Database encryption + PII log redaction |
| S-205 | Clipboard cleared after copying sensitive data where feasible | ⬜ | 2 | Clipboard utility |
| S-206 | Notification text is generic (no amounts/names until unlocked) | ✅ | 2 | Generic notification titles and secure lock barrier |
| S-207 | Offline cache: no sensitive data accessible without PIN/biometric | ✅ | 2 | Guarded by `AppLockManager` (60s timer) & BiometricAuthManager |
| S-208 | Room with SQLCipher key in Keystore | ✅ | 2 | Implemented with `DatabasePassphraseManager` (KeyStore AES-256-GCM) in `DatabaseModule` |

---

## Phase 3 — Payments + verify.et

| ID | Item | Status | Phase | Notes |
|----|------|--------|-------|-------|
| S-301 | Unique index on `(provider, normalized_reference)` in DB | ⬜ | 3 | Prevents duplicate receipt reuse |
| S-302 | `settlementAccount` always sent to verify.et | ⬜ | 3 | |
| S-303 | Webhook: raw body preserved for HMAC verification | ⬜ | 3 | |
| S-304 | Webhook: HMAC-SHA256 verified with constant-time comparison | ⬜ | 3 | |
| S-305 | Webhook: timestamp tolerance ≤ 5 minutes | ⬜ | 3 | |
| S-306 | Webhook: deduplicated on `X-Webhook-Event-Id` | ⬜ | 3 | |
| S-307 | Webhook: returns 2xx fast, processes on queue | ⬜ | 3 | |
| S-308 | Idempotency-Key on all mutating API calls | ⬜ | 3 | |
| S-309 | Decision engine: all branches unit-tested | ✅ | 3 | decision-engine.spec.ts — 12 test cases |
| S-310 | Contract tests: recorded verify.et fixtures (success, not-found, mismatch, 402, 429, 503) | ⬜ | 3 | Never call live API in tests |
| S-311 | Ledger: no UPDATE or DELETE grants for app DB role | ⬜ | 3 | |
| S-312 | Ledger: double-entry, append-only, corrections by reversing entries | ⬜ | 3 | |
| S-313 | Ledger: nightly balance check (sum debits = sum credits) | ⬜ | 3 | |
| S-314 | Receipt image: magic bytes + MIME + size validated before forward | ⬜ | 3 | Feature-flagged |
| S-315 | Receipt image: forwarded to verify.et only, not stored raw on public path | ⬜ | 3 | |
| S-316 | 402 / low-balance alert to admins before credits run out | ⬜ | 3 | |
| S-317 | Duplicate receipt reuse impossible — automated test | ⬜ | 3 | |
| S-318 | Forged webhook signature rejected — automated test | ⬜ | 3 | |
| S-319 | Webhook replay (same event-id) idempotent — automated test | ⬜ | 3 | |
| S-320 | Input validation: provider-specific regex/length checks before calling verify.et | ⬜ | 3 | |
| S-321 | App cannot set payment status — backend recalculates from DB | ⬜ | 3 | |
| S-322 | Velocity limits per member (attempts/hour, failed/day) | ⬜ | 3 | |

---

## Phase 4 — Loans & Statements

| ID | Item | Status | Phase | Notes |
|----|------|--------|-------|-------|
| S-401 | Loan instalment auto-allocation tested with edge cases | ⬜ | 4 | |
| S-402 | PDF statement export: no injection via member data | ⬜ | 4 | |
| S-403 | Overpayment/refund flow audit-logged and staff-approved | ⬜ | 4 | |

---

## Phase 5 — Hardening & Launch

| ID | Item | Status | Phase | Notes |
|----|------|--------|-------|-------|
| S-501 | External security review / penetration test completed | ⬜ | 5 | Before public launch |
| S-502 | Load test: sustained traffic within 2GB server budget | ⬜ | 5 | |
| S-503 | OWASP Dependency Check / osv-scanner in CI, no High/Critical open | ⬜ | 5 | |
| S-504 | Play Store Data Safety form completed accurately | ⬜ | 5 | |
| S-505 | Play Store Financial Services declaration filed | ⬜ | 5 | |
| S-506 | Privacy policy URL live and linked in app | ⬜ | 5 | |
| S-507 | Account deletion flow working end-to-end | ⬜ | 5 | |
| S-508 | Backup restore test: documented RPO/RTO | ⬜ | 5 | |
| S-509 | verify.et API key rotated before launch, expiry set | ⬜ | 5 | |
| S-510 | Webhook secret rotated before launch | ⬜ | 5 | |
| S-511 | Nightly ledger balance check running in production | ⬜ | 5 | |
| S-512 | Legal review: NBE directives, PDPP compliance confirmed | ⬜ | 5 | See plan Section 16 |

---

*Last updated: Phase 0 complete.*  
*Maintainer: update this file in every PR touching auth, payments, ledger, or webhooks.*
