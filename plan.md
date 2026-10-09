# Vision for a Better Life SLA — Android App: Engineering Plan

---

## 1. Product Summary and Assumptions

**Product:** Android app for *Vision for a Better Life SLA*, a small-to-medium financial organisation. Members use it to manage their account, make payments (contributions, loan repayments, fees) and see their financial history. Payments are made by the member through Ethiopian banks and wallets (CBE, Telebirr, etc.), and the app **verifies each payment receipt** through **verify.et** before crediting the member's account.

**Assumptions (confirm with the business owner. Where unsure, build behind a config flag):**

| # | Assumption | Impact if wrong |
|---|---|---|
| A1 | "SLA" is a savings-and-loan association: members, savings/contributions, loans, repayments. | Domain model (Section 11) changes. |
| A2 | The organisation **receives** money in its own bank and wallet accounts. It does not run a payment gateway. Members pay outside the app (bank app, Telebirr) and submit proof of payment. | If an in-app payment gateway is wanted, verify.et is the wrong tool. It only verifies receipts and does not collect or settle money. |
| A3 | Android only, phones from Android 8 (API 26) up. Many users have low-end devices and unstable data. | Drives the offline and performance requirements. |
| A4 | Two user types: **Members** (mobile app) and **Staff/Admin** (web admin panel, a separate small project, minimal version in Phase 3). | Staff features need a place to live. |
| A5 | Languages: English, Amharic (አማርኛ), Afaan Oromoo. | Localisation is built in from day 1. |

---

## 2. Non-Negotiable Architecture Decision

```
┌──────────────┐   HTTPS + cert pinning   ┌──────────────────┐   x-api-key   ┌──────────────┐
│ Android App  │ ───────────────────────► │  VBL Backend API │ ────────────► │  verify.et   │
│ (no secrets) │ ◄─────────────────────── │  (your server)   │ ◄──────────── │  REST+webhook│
└──────────────┘      JWT (short-lived)   └──────────────────┘  webhook HMAC └──────────────┘
                                                   │
                                          PostgreSQL + local encrypted file storage
                                          (single 2 GB server, rootless Podman, see 3.1)
```

**Rule 1. The verify.et API key is NEVER in the APK.** Anything inside an APK can be extracted, so a leaked key means someone burns your verification credits or reads your history. The app only talks to **our backend**. The backend holds the key in a secrets manager and calls verify.et.

**Rule 2. The backend, not the app, decides whether money counts.** The app can only say "I paid, here is my reference." The backend verifies, checks business rules, and writes to an append-only ledger.

**Rule 3. Every state change is server-side and auditable.** The app is a view and a form.

---

## 3. Tech Stack

### Android app
| Concern | Choice |
|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Architecture | Clean Architecture + MVVM/MVI, unidirectional data flow, multi-module |
| DI | Hilt |
| Async | Coroutines + Flow |
| Networking | Retrofit + OkHttp + kotlinx.serialization, certificate pinning |
| Local DB | Room + SQLCipher (encrypted) |
| Prefs / tokens | DataStore + Android Keystore (EncryptedFile or Tink) |
| Background | WorkManager (retry of submissions, sync) |
| Navigation | Navigation-Compose (type-safe routes) |
| Images | Coil |
| Camera / scan | CameraX + ML Kit (barcode and text recognition) |
| Auth | Phone OTP + PIN + BiometricPrompt |
| Push | Firebase Cloud Messaging |
| Crash/analytics | Firebase Crashlytics (no PII in logs or analytics) |
| Logging | Timber (release tree is a no-op for sensitive tags) |
| Quality | detekt, ktlint, Android Lint, LeakCanary (debug) |
| Testing | JUnit5, MockK, Turbine, Compose UI tests, Espresso, Paparazzi/Roborazzi screenshots |

`minSdk 26`, `targetSdk` = latest stable required by Google Play, Gradle version catalog (`libs.versions.toml`), R8 full mode.

### Backend (sized for the 2 GB rootless-Podman server)
**NestJS (TypeScript, Fastify adapter)** + **PostgreSQL 16** only. There is **no Redis and no separate queue service**. With 2 GB shared by everything, every extra daemon hurts. Instead:
- Job queue and retries: a Postgres table polled with `SELECT ... FOR UPDATE SKIP LOCKED` (e.g. `pg-boss`), run inside the API container or one small worker container.
- Rate limiting and idempotency: Postgres tables (or in-process cache plus DB), which is fine at SMB scale.
- Receipt images / KYC documents: files on the server's disk, encrypted, outside the web root, referenced by DB rows (storage is unlimited, so keep originals, but follow the retention policy).
- Use a stack your team knows. The contract in Section 12 is what matters. Spring Boot is **not** advised on 2 GB (the JVM is memory-hungry).

### 3.1 Hosting and deployment on your server (rootless Podman)

**Containers (one `compose`/Quadlet stack):**

| Container | Purpose | RAM budget (limit) |
|---|---|---|
| `caddy` | Reverse proxy, automatic HTTPS (Let's Encrypt) | ~50 MB |
| `api` | NestJS backend (HTTP + webhook + job runner) | 350 MB (`--memory 400m`, Node `--max-old-space-size=300`) |
| `postgres` | PostgreSQL (tuned: `shared_buffers=256MB`, `max_connections=30`) | 600 MB |
| `admin-web` (Phase 3+) | Staff web panel (static files served by Caddy) | ~0 |
| `backup` (cron job container) | Encrypted dump + off-site upload | transient |

That is roughly 1.1 GB for the stack, leaving headroom for the OS. **Add 2 GB of swap** as a safety net, and set a memory limit on every container.

**Rootless Podman specifics the agent must handle:**
1. **Ports below 1024 are blocked for rootless.** Check first who owns ports 80/443 on your server. Choose one:
   - If nothing else uses them: allow with `sysctl net.ipv4.ip_unprivileged_port_start=80` (needs root once), then `caddy` binds 80/443.
   - If the host's existing web server (Apache/Nginx/cPanel) owns 80/443: keep it, and **reverse-proxy `https://api.<yourdomain>` to `127.0.0.1:8080`** (the Caddy or API port) from that web server. TLS is then terminated by the host's server, so make sure it sends `X-Forwarded-For/Proto` and that the API trusts only that proxy.
2. **Run as a systemd user service.** Use **Quadlet** (`~/.config/containers/systemd/*.container`, Podman 4.4+) or `podman generate systemd`, then `loginctl enable-linger <user>` so containers start on boot and survive logout.
3. Postgres data in a **named volume** or bind mount with `:Z` (SELinux) and correct `--userns=keep-id` permissions. Test restarts.
4. Containers on a private Podman network. **Only the reverse proxy port is exposed.** Postgres is never published to the host.
5. Pin image **digests** (not `latest`), rebuild monthly, scan with `trivy`.
6. Config and secrets via **Podman secrets** (`podman secret create`) or root-only env files with mode `600`, never baked into images and never in git.

**Server hardening (non-negotiable on a single internet-facing box):**
- Domain with DNS and a valid HTTPS certificate. **The verify.et webhook requires a public HTTPS URL**, so confirm the server is reachable from the internet before Phase 3.
- Firewall: allow only 80/443 and SSH. SSH keys only, no password login, `fail2ban`, non-default user, unattended security updates.
- Check what else shares this server. **Other sites on the same host are a risk to a financial system.** Isolate under a dedicated OS user, and consider moving the financial backend to its own VPS before real money flows (a small VPS is cheap).
- **Backups (the biggest single-server risk):** nightly `pg_dump` plus the files directory, encrypted with `age` or `gpg`, uploaded **off-server** (a second provider or storage bucket), 30-day retention, and a **restore test every month**. Also enable Postgres WAL archiving if you can afford the effort. Document RPO/RTO honestly: one server means downtime during maintenance or failure.
- Monitoring: Uptime Kuma (or an external free uptime monitor) pinging `/health`, disk, RAM and swap alerts to Telegram/email, `podman stats` baseline, log rotation.

**CI/CD to this server:** GitHub Actions builds the image, pushes it to a registry (GHCR), and the server pulls it via a restricted deploy key and `podman auto-update` (or a small `deploy.sh`). Migrations run as a separate one-shot step before the new API starts, with rollback to the previous image digest.

**When to outgrow it:** more than a few thousand active members, or sustained RAM > 80% / DB > 10 GB. The architecture (stateless API plus Postgres) moves to a bigger VPS or managed DB without code changes.

> Network note: some Ethiopian providers' public receipt pages only respond from certain networks. verify.et handles that on its side, so your server only talks to `verify.et`. Measure latency from your server once, early.

---

## 4. verify.et Integration Specification

Source of truth: https://verify.et/docs/get-started and https://verify.et/docs/api. **Before coding, the agent must read https://verify.et/docs/sdk for the Node.js webhook receiver example**, and re-check the docs for changes.

### 4.1 Facts about the API
- **Base URL:** `https://verify.et`
- **Auth:** header `x-api-key: VERIFY_BANK_ET_...`. Create the key in the verify.et dashboard with only `verification:write` and `verification:read` permissions, **set an expiry**, and use a separate key for dev/staging/prod.
- **Submit:** `POST /api/verify?waitMs=5000`
  - Headers: `Content-Type: application/json`, `x-api-key`, **`Idempotency-Key: <our paymentAttemptId>`**
  - `200` = finished result in the body. `202` = queued. Store `requestId` and wait for the webhook (or poll `GET /api/verify/:requestId`).
  - Always handle **both** 200 and 202.
- **Per-provider fields:**

| Provider | `bank` | Required fields |
|---|---|---|
| CBE | `cbe` | `reference` = Receipt ID (or a `mbreciept.cbe.com.et` link). Older `FT…` references also need an 8-digit `suffix`. |
| Telebirr | `telebirr` | `transactionNumber` (or `reference`) |
| CBE Birr | `cbebirr` | `receiptNumber` + `phone` |
| Bank of Abyssinia | `boa` | `referenceNumber` + 5-digit `accountSuffix` |
| M-Pesa | `mpesa` | `transactionNumber` |
| Dashen / Awash / Siinqee | `dashen` / `awash` / `siinqee` | `referenceNumber` |
| Kaafi / Coopay Ebirr | `kaafiebirr` / `coopayebirr` | receipt link or token (not just the SMS ref) |
| Zemen | n/a | **Not supported** for direct submission. Show "manual review". |

- **Settlement account matching:** send `settlementAccount` (our organisation's receiving account/phone) so a valid receipt paid to **someone else** is rejected. The response includes `settlementAccountMatch.matched`. **Always send it.**
- **Result fields:** `status` (`success` / not found / failed), `verified`, `amount`, `currency` (ETB), `senderName`, `receiverName`, `receiverAccount` (may be masked), `referenceNumber`, `timestamp`, `confirmationHistory` (`confirmedBefore`, `confirmationCount`), `settlementAccountMatch`.
- **Webhook** (preferred for final result): event `verification.completed`. Headers `X-Webhook-Signature: sha256=<hex>`, `X-Webhook-Timestamp`, `X-Webhook-Event-Id`. Signature = **HMAC-SHA256 over `timestamp + "." + rawBody`** with the `whsec_…` secret. Up to 5 retries (immediately, 30s, 2m, 8m, 30m) with the same event id. Reply `2xx` fast, then process. During secret rotation the header may contain two `sha256=` values, so accept either.
- **Receipt image:** `multipart/form-data` with part `image` (JPEG/PNG/WebP ≤ 8 MB) plus `Idempotency-Key`. This is an **optional paid add-on** with separate image credits and it must be switched on in the dashboard. A credit is spent even if reading later fails.
- **Errors to handle:** `401` bad key, `403` missing permission, `402 INSUFFICIENT_CREDITS`, `409` idempotency conflict, `422` invalid fields, `429` rate limit (honour `Retry-After`), `503` queue unavailable.
- **Pricing/plan reality check (as of the docs fetched Oct 2026):** the free Starter tier is limited (about 200 verifications/month). Dev Plus is 500 ETB/month for 5,000 verifications. The "Verify Pro (App)" plan **does not include API access**. We need a **Developer API plan**. Credits are account-wide, so monitor the balance and alert before it runs out. Check the pricing page again before launch.

### 4.2 Backend module `verification` (agent: implement exactly this)

1. `VerifyEtClient`: a thin typed wrapper (timeouts 10s, 1 retry on network error only, never retry on 4xx, circuit breaker, structured logs **without** API key or full receipt numbers).
2. `POST /v1/payments/:id/submit` (called by the app), which:
   - validates input per provider (regex and length checks, Telebirr/CBE formats),
   - **enforces uniqueness** on `(provider, normalized_reference)` with a DB unique index **before** calling verify.et (this stops the same receipt being used twice, including by two different members),
   - creates `payment_attempt` with status `SUBMITTED`,
   - calls verify.et with `Idempotency-Key = payment_attempt.id` and `settlementAccount` from config,
   - on `200` processes the result immediately, on `202` stores `requestId` and sets status `VERIFYING`.
3. `POST /webhooks/verify-et`, which:
   - reads the **raw body**, checks the HMAC with a constant-time comparison, **rejects if the timestamp is older than 5 minutes**,
   - dedupes on `X-Webhook-Event-Id` (table `webhook_events`),
   - returns `204` immediately, then handles the event on the queue.
4. A **reconciliation job** every 5 minutes polls `GET /api/verify/:requestId` for any attempt stuck in `VERIFYING` > 3 minutes, so a missed webhook never leaves a member hanging.
5. **Decision engine** (pure function, fully unit-tested):

```
CREDIT automatically only if ALL true:
  result.status == success AND verified == true
  AND currency == ETB
  AND settlementAccountMatch.matched == true AND not ambiguous
  AND result.amount matches the expected amount (exact for fixed fees; >= for open contributions, per rules)
  AND receipt timestamp is within the allowed window (e.g. not older than N days and not before the invoice/loan date)
  AND this reference was never credited in OUR database
  AND confirmationHistory.confirmedBefore == false  (or confirmedBefore but only by OUR account/request)
  AND member daily/monthly limits not exceeded
ELSE if provider not found / not yet available → status PENDING_RETRY (auto re-check up to N times over 24h)
ELSE if amount/receiver mismatch, duplicate, or suspicious → MANUAL_REVIEW (staff queue), notify the member neutrally
ELSE → REJECTED with a clear, safe reason code.
```
   Never show the member raw provider data of other people (full sender names or account numbers). Show only what they need.
6. **Ledger:** crediting writes **double-entry, append-only** rows in one DB transaction together with the attempt's status change. Corrections are made by **reversing entries**, never updates or deletes.
7. **Image path:** the app uploads the image to **our backend** (never directly to verify.et). The backend validates type, size and magic bytes, strips EXIF, forwards it to verify.et as multipart with the `Idempotency-Key`, and stores the original in private object storage with a retention policy. Feature-flag this path, because it costs image credits.
8. **Credit monitoring:** `402` or a low balance triggers an alert to admins and a friendly "verification temporarily delayed" state for members. Payments are queued, not lost.

### 4.3 Payment attempt state machine
```
DRAFT → SUBMITTED → VERIFYING → VERIFIED_CREDITED
                      │  ├──► PENDING_RETRY ──► (re-check) ──┐
                      │  ├──► MANUAL_REVIEW ──► APPROVED / REJECTED (staff, audit-logged)
                      └──► REJECTED   |   EXPIRED (no result in 72h → staff queue)
```
The app shows only these states, with plain-language labels (Section 10).

---

## 5. Project Structure (multi-module)

```
:app                         // entry, DI graph, navigation host
:core:designsystem           // theme, typography, components, icons
:core:ui                     // shared composables, UiState, error views
:core:common                 // Result, dispatchers, formatters (ETB, dates, Ethiopian calendar)
:core:network                // Retrofit, interceptors, pinning, token refresh authenticator
:core:database               // Room + SQLCipher
:core:datastore              // settings, secure token storage
:core:security               // Keystore, biometric, integrity checks, root/tamper signals
:core:model                  // domain models
:feature:onboarding          // splash, language, OTP, KYC, PIN/biometric setup
:feature:home                // dashboard
:feature:payments            // pay, submit reference/scan, status tracking, receipts
:feature:loans               // loan products, apply, schedule, repayments
:feature:savings             // balances, contributions, statements
:feature:profile             // profile, security settings, language, help
:feature:notifications
:feature:support
```
Rules: features never depend on each other, only on `core`. Domain layer has no Android imports. Repositories are interfaces in the domain layer and implemented in `data`.

---

## 6. Features by Priority

**MVP (Phases 1–3):**
- Phone-number sign-up/login with OTP, PIN, biometric unlock, device binding
- KYC: name, DOB, address, national ID (Fayda/Kebele/passport) photo + selfie, status tracking (staff approves)
- Home: balances (savings, loan outstanding), next due date, recent activity
- **Make a payment:** choose purpose (contribution / loan repayment / fee) → see organisation's receiving accounts (copy button, deep link to Telebirr/CBE app where possible) → enter reference **or** scan receipt QR/screenshot → live status tracking → receipt in history
- Transaction history with filters, PDF/image statement export
- Loan: view products, apply, view schedule and repayments
- Push + in-app notifications (payment verified/rejected, due reminders)
- Profile, change PIN, language, logout, delete-account request flow

**Later (Phase 5+):** group/meeting features, dividends, referrals, savings goals, in-app chat support, USSD/SMS fallback, Ethiopian calendar toggle, tablet layout.

---

## 7. Key User Flows (for UI implementation and UI tests)

1. **First launch:** language pick → short value screens (max 3, skippable) → phone number → OTP → create PIN → enable biometrics (optional) → KYC (can be done later, but payments are limited until approved).
2. **Pay (the core flow, optimise heavily):**
   1. Tap *Pay* → pick purpose and amount (prefilled for loan instalments).
   2. Screen shows organisation account(s) with large **Copy** buttons and a warning ("Pay only to these accounts").
   3. Member pays in their bank/Telebirr app, then returns.
   4. *"I've paid"* → choose provider → enter reference (field is smart: pastes SMS/receipt link and auto-extracts) or **Scan receipt** (QR first, screenshot fallback).
   5. Confirmation screen, then **status screen** with a timeline (Submitted → Verifying → Credited). It works if the app is closed, and a push arrives on completion.
   6. Result: success receipt (shareable) or a clear next step (retry / contact support / under review).
3. **Returning user:** biometric or PIN → Home. Session auto-locks after 60s in background (configurable).
4. **Failure handling:** every error screen says what happened, whether money is safe, and what to do next.

---

## 8. Payment Anti-Fraud Rules (backend + app)

- Unique index on `(provider, normalized_reference)`. A reference is usable **once, ever**.
- Always require `settlementAccountMatch.matched`.
- Compare amount, currency and timestamp window. Reject receipts older than the invoice.
- Use verify.et `confirmationHistory` as a second signal, and our DB as the authority.
- Velocity limits per member (attempts/hour, failed attempts/day). After N failures, require staff review.
- Name check is a **soft signal** only (sender name vs member name can legitimately differ, such as a family member paying).
- Staff manual approval needs a **reason, a second approver above a threshold**, and is fully audit-logged.
- Large or unusual payments go to review automatically.
- Never trust anything sent by the app about amounts or status. Only trust verify.et results and our DB.

---

## 9. Security Requirements

Reference standards: **OWASP MASVS (L1 + L2 for the auth and payment sections)** and OWASP MASTG, plus OWASP API Security Top 10 for the backend. The agent must keep a `docs/SECURITY_CHECKLIST.md` and tick items off per phase.

### 9.1 Android app
- **No secrets in the app:** no API keys, no verify.et key, no signing keys in source, `BuildConfig`, resources or Gradle files. Secrets scanning in CI (gitleaks).
- **Transport:** HTTPS only. `network_security_config.xml` with `cleartextTrafficPermitted=false`, **certificate pinning** (pin the leaf's public key hash and a backup pin, with a documented rotation process so a cert change does not brick the app).
- **Auth:** short-lived access token (≤15 min) + rotating refresh token stored encrypted with a **Keystore-backed key** (hardware-backed/StrongBox when available). Token refresh through an OkHttp `Authenticator` with a single-flight lock.
- **App lock:** PIN (hashed server-side verification or Keystore-bound key, never plain text locally) + `BiometricPrompt` (BIOMETRIC_STRONG), lockout after 5 wrong PINs with backoff, auto-lock on background.
- **Device integrity:** **Play Integrity API** attestation checked **server-side** on login and sensitive actions. Root/emulator/hook/debugger signals are used as **risk signals** (step-up auth or limits), not as the only defence.
- **Screen protection:** `FLAG_SECURE` on PIN, KYC, balances and receipt screens. Hide content in the recents thumbnail.
- **Data at rest:** Room with SQLCipher (key in Keystore), `allowBackup=false`, `dataExtractionRules` excluding everything, no sensitive data in logs, clipboard, or notifications (notification text is generic: "Payment update", with details only after unlock). Clear the clipboard after copying sensitive data where feasible.
- **Code hardening:** R8 full minification/obfuscation, release builds `debuggable=false`, strip logs, verify signature at runtime as a risk signal. Play App Signing enabled.
- **Components:** every `Activity/Service/Receiver/Provider` has `exported="false"` unless required. Validate deep links and intents. Use `PendingIntent.FLAG_IMMUTABLE`. WebViews are avoided. If one is unavoidable, disable JS and file access and use an allow-list.
- **Input:** validate everything client-side for UX, and **again server-side for security**. Use parameterised Room queries.
- **Permissions:** least privilege (camera and notifications only, requested in context with a rationale). **No SMS read permission** (it is also restricted by Google Play policy).
- **Dependencies:** pinned versions, Dependabot or Renovate, OWASP dependency-check/`osv-scanner` in CI.
- **Privacy:** minimal data collection, no PII in analytics, in-app privacy policy and consent, working account/data deletion path.

### 9.2 Backend
- OTP: 6 digits, 5-min expiry, rate-limited per phone and per device, resend cooldown, anti-SIM-swap signals (new device = step-up + notification to old device).
- Passwords/PINs: Argon2id. Never log OTPs, PINs or tokens.
- JWT with short TTL, refresh rotation with reuse detection (revoke the whole family on reuse), device-bound sessions, remote logout.
- RBAC for staff (member / teller / accountant / admin), **least privilege, MFA for all staff**, full audit trail (who, what, when, from where).
- Rate limiting and WAF on all endpoints, strict stricter limits on `/auth/*` and `/payments/*/submit`.
- Validate and sanitise all input (DTO schemas), parameterised queries only, strict CORS (admin web only), security headers, request-size limits.
- Encryption in transit (TLS 1.2+) and at rest (DB + object storage). Sensitive columns (national ID number) application-level encrypted.
- Secrets in a secrets manager or env injected at deploy. **Never in git.** Separate keys per environment. Rotate verify.et key and webhook secret on a schedule and on any suspicion of leak.
- Webhook endpoint: HMAC check, timestamp tolerance, dedupe, no side effects before verification.
- Backups: daily encrypted, tested restores, defined RPO/RTO. Immutable audit logs.
- Monitoring/alerting: failed-login spikes, webhook signature failures, verification error rate, credit balance, ledger imbalance check (nightly: sum of debits = sum of credits).
- Pen-test or at minimum an external security review **before public launch** and after major changes.

---

## 10. UI / UX / Usability

**Principles:** trust, clarity and speed on slow phones. People are handling their money, so the interface should feel calm, obvious and honest.

- **Design system first** (`:core:designsystem`): Material 3 with a custom token set (colour, type, spacing 4/8dp grid, shape, elevation). Light and dark themes. A calm, trustworthy palette with a strong primary, a single accent, and semantic colours (success/warning/error) that **never rely on colour alone** (always pair with icon and text).
- **Typography:** a font with full **Ge'ez (Amharic) and Latin** coverage (e.g. Noto Sans Ethiopic + a Latin family). Test the longest Amharic and Oromo strings for overflow, since text expands.
- **Localisation:** all strings in resources, never hard-coded. English / አማርኛ / Afaan Oromoo, runtime language switch without restart. Plurals and number/date formats via locale. **Amounts shown as `ETB 1,500.00`** consistently. Optional Ethiopian-calendar display.
- **Accessibility (WCAG 2.2 AA mindset):** contrast ≥ 4.5:1, touch targets ≥ 48dp, TalkBack labels and logical focus order, dynamic font scale up to 200%, no information in colour only, haptics and visual (not sound-only) feedback.
- **Low-end and low-bandwidth reality:**
  - Cold start < 2s on a 2GB-RAM device, APK/AAB small (target < 20 MB download), use baseline profiles.
  - Cache the last known balances and history (clearly marked "last updated"), skeleton loaders instead of spinners, pagination, image compression before upload (target ≤ 1 MB).
  - **Offline-aware:** the payment status screen and history work offline from cache. Submissions queue with WorkManager and show "will send when you're online". The server's idempotency key makes retries safe.
- **Navigation:** bottom bar with 4 items (Home, Pay, History, Profile), a big primary *Pay* action. Max 3 taps to start a payment. One primary action per screen.
- **Forms:** one question at a time for KYC, inline validation as the user types (not only on submit), correct keyboard types, paste-friendly reference field that auto-cleans spaces and extracts from pasted SMS/links, progress saved between sessions.
- **Feedback states for EVERY screen:** loading (skeleton), empty (helpful message and action), error (what happened, is money safe, what to do, Retry), offline, success. Specify these in each feature's design before coding.
- **Microcopy:** plain, human, no jargon, no blame. Example: not *"Verification failed: ERR_422"* but *"We couldn't confirm this payment yet. Your money is safe. Check the reference number and try again, or contact us."*
- **Trust cues:** organisation name and licence/registration info in About, clear fee display **before** confirming, receipts with a reference ID members can quote to support, clear "Pay only to these accounts" anti-scam warnings, security tips on first launch.
- **Motion:** subtle (150–250ms), respect the system "reduce motion" setting.
- **Deliverables from the agent in Phase 0:** a Figma-free alternative is fine, meaning a `docs/ui-spec.md` with the token table and a Compose preview gallery of components (`@Preview` for light/dark/Amharic/large font). Review with 5 real members, run a usability test with the actual user group, and iterate.

---

## 11. Data Model (core tables, backend)

`jobs` (Postgres-backed queue, replaces Redis), `rate_limits`, `members`, `devices`, `sessions`, `otp_challenges`, `kyc_documents`, `accounts` (savings), `loan_products`, `loans`, `loan_schedule_items`, `invoices` (what is owed), `payment_attempts`, `verification_requests` (verify.et `requestId`, raw sanitised result JSON, status), `webhook_events` (unique `event_id`), `ledger_entries` (append-only, double-entry), `staff_users`, `roles`, `audit_logs`, `notifications`, `org_receiving_accounts` (provider, account/phone, display name, active), `config_flags`.

Key constraints:
- `payment_attempts`: unique `(provider, normalized_reference)`, with a state machine enforced in code **and** a CHECK on allowed statuses.
- `ledger_entries`: no UPDATE or DELETE grants for the app DB role.
- Money stored as **integer minor units (cents/santim)** or `NUMERIC(14,2)`, never floating point.

---

## 12. Backend API Contract (what the app calls)

All under `/v1`, JSON, `Authorization: Bearer`, every mutating call carries `Idempotency-Key`. Standard error body: `{ "error": { "code": "...", "message": "...", "retryable": true|false } }`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/auth/otp/request`, `/auth/otp/verify` | Phone login |
| POST | `/auth/pin`, `/auth/refresh`, `/auth/logout` | PIN, tokens |
| POST | `/devices/attest` | Play Integrity verdict |
| GET/PUT | `/me`, `/me/kyc` | Profile, KYC upload and status |
| GET | `/dashboard` | Balances, next due, recent |
| GET | `/org/receiving-accounts` | Where to pay |
| POST | `/payments` | Create attempt (purpose, amount, invoice) |
| POST | `/payments/{id}/submit` | Provider + reference (or multipart image) |
| GET | `/payments/{id}` | Status + timeline |
| GET | `/payments?cursor=` | History |
| GET | `/loans`, `/loans/{id}/schedule`, POST `/loans/applications` | Loans |
| GET | `/statements?from&to` | Statement export |
| GET/POST | `/notifications`, `/devices/push-token` | Notifications |
| POST | `/webhooks/verify-et` | **verify.et only**, signature-checked, not for the app |

Publish an **OpenAPI 3** spec; generate the Android client or DTOs from it so the contract can't drift. Version the API (`/v1`) and support N-1 app versions with a **force-update** gate.

---

## 13. Quality, CI/CD, Observability

- **Branching:** trunk-based with short-lived PRs, conventional commits, required reviews.
- **CI (GitHub Actions or similar) on every PR:** build, ktlint, detekt, Android Lint, unit tests, screenshot tests, dependency + secret scanning. Nightly: instrumented UI tests on emulator (min API 26 and the latest).
- **Test pyramid:** heavy unit tests (decision engine, ViewModels, mappers, formatters), integration tests for repositories and Retrofit with MockWebServer, Compose UI tests for the Pay flow, and **backend contract tests that replay recorded verify.et responses** (success, not found, masked receiver, mismatch, 402, 429, 503). **Never hit the live verify.et API in automated tests.**
- **Verify.et sandbox reality:** there is no separate sandbox in the docs, so use a mock server (e.g. WireMock/MSW) locally and in CI, and a **single manually-run smoke test** against production with a real small payment in staging.
- **Release:** signed AAB through Play App Signing, internal → closed → open testing → staged rollout (5% → 20% → 50% → 100%) with crash-free-user gate (> 99.5%). Remote kill-switch/feature flags for risky features.
- **Play Store compliance:** complete the Data Safety form accurately, the Financial Services declaration, a public privacy policy URL, no restricted permissions. Follow Google Play's policies for loan/personal-finance apps, including clear disclosure of terms, fees and the licensed entity.
- **Observability:** Crashlytics (no PII), backend structured logs with correlation ID (= `Idempotency-Key`/`requestId`), metrics and dashboards (verification latency, success rate by provider, credit balance, queue depth), alerts on-call.

---

## 14. Phased Roadmap with Acceptance Criteria

| Phase | Scope | Done when |
|---|---|---|
| **0. Foundations** (≈1 wk) | Repo, multi-module skeleton, design system, CI, secrets scanning, OpenAPI skeleton, `SECURITY_CHECKLIST.md`, `ui-spec.md` | App builds in CI, lint/tests green, previews render in 3 languages |
| **1. Identity** (≈2 wks) | OTP login, PIN, biometrics, device binding, token rotation, Play Integrity, app lock | Pen-test checklist for auth passes. Tokens verified encrypted. Brute-force locked out |
| **2. Core read features** (≈2 wks) | Dashboard, history, profile, notifications, offline cache, KYC upload | Works offline from cache. Accessibility audit passed |
| **3. Payments + verify.et** (≈3 wks) | Section 4 fully, Pay flow, status timeline, manual-review queue (minimal admin web), reconciliation job | All verify.et cases covered by contract tests. Duplicate receipt reuse impossible (tested). Webhook replay and forged signature rejected (tested). 202 and 200 paths both work |
| **4. Loans & statements** (≈2 wks) | Products, application, schedule, repayment linking to invoices, PDF statements | Instalment payment auto-allocates correctly. Ledger balances nightly |
| **5. Hardening & launch** (≈2 wks) | External security review, load test, usability test with real members, store listing, staged rollout | No open High/Critical findings. Crash-free > 99.5% in closed beta |

---

## 15. Instructions for the IDE Agent

1. Read this whole file first. If something is ambiguous, **ask**, don't guess, especially about money rules.
2. Work **one phase, one feature at a time**. Each PR is small, includes tests, and updates docs.
3. Never put a secret, API key, token or real receipt number in code, tests, logs or commits. Use `.env.example` with placeholders.
4. Never call the live verify.et API from tests. Use recorded fixtures and a mock server.
5. All user-facing text goes in string resources for all 3 languages (mark missing translations with `TODO(translation)`).
6. Every screen implements loading / empty / error / offline states and has previews and a UI test.
7. Money is integer minor units or `BigDecimal`. Never `Double`/`Float`.
8. When touching auth, payments, ledger or the webhook, add or update tests **first** and note the change in `SECURITY_CHECKLIST.md`.
9. Do not add dependencies without stating why. Prefer AndroidX/Google/first-party libraries.
10. Definition of Done: compiles, lint clean, tests pass, security checklist items for that feature ticked, accessibility checked, docs updated.

---

## 16. Open Questions to Settle Before Phase 3

1. Exact legal form and regulator of the SLA, and what licensing/consumer-protection rules apply to lending and savings apps in Ethiopia. **Get a lawyer's confirmation** (also on the Personal Data Protection Proclamation and National Bank of Ethiopia directives) before launch.
2. Which receiving accounts/wallets will the organisation publish, and which providers must be supported on day 1 (suggested: Telebirr + CBE first)?
3. Business rules: fixed vs flexible amounts, grace periods, late fees, how a payment is allocated across loan instalments, refund/overpayment handling.
4. Staff workflow: who approves KYC and manual reviews, and the approval thresholds.
5. Budget for verify.et plan (API plan + image-processing credits) and expected monthly verification volume.
6. Data retention periods (receipts, KYC documents, audit logs) and the account deletion policy.
7. Support channel (phone, Telegram, in-app) and service-hour promises shown in the app.

---

*verify.et facts in Section 4 come from https://verify.et/docs/get-started, https://verify.et/docs/api and https://verify.et/pricing as read on 2026-10-09. The API and pricing can change, so re-check them before each release.*