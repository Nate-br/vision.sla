/**
 * PaymentProvider enum — mirrors Android core:model and the verify.et `bank` field.
 *
 * All providers from plan Section 4.1.
 */
export enum PaymentProvider {
  CBE         = 'cbe',
  TELEBIRR    = 'telebirr',
  CBE_BIRR    = 'cbebirr',
  BOA         = 'boa',
  MPESA       = 'mpesa',
  DASHEN      = 'dashen',
  AWASH       = 'awash',
  SIINQEE     = 'siinqee',
  KAAFI_EBIRR = 'kaafiebirr',
  COOPAY_EBIRR= 'coopayebirr',
  /** Not supported by verify.et — manual review only */
  ZEMEN       = 'zemen',
}

export const UNSUPPORTED_PROVIDERS = [PaymentProvider.ZEMEN];

/**
 * Payment attempt status — state machine from plan Section 4.3.
 *
 * The app only DISPLAYS these. The backend is the single source of truth.
 */
export enum PaymentStatus {
  DRAFT             = 'DRAFT',
  SUBMITTED         = 'SUBMITTED',
  VERIFYING         = 'VERIFYING',
  PENDING_RETRY     = 'PENDING_RETRY',
  VERIFIED_CREDITED = 'VERIFIED_CREDITED',
  MANUAL_REVIEW     = 'MANUAL_REVIEW',
  APPROVED          = 'APPROVED',
  REJECTED          = 'REJECTED',
  EXPIRED           = 'EXPIRED',
}

/** Terminal states — no further automatic transitions */
export const TERMINAL_STATUSES: PaymentStatus[] = [
  PaymentStatus.VERIFIED_CREDITED,
  PaymentStatus.APPROVED,
  PaymentStatus.REJECTED,
  PaymentStatus.EXPIRED,
];

/**
 * Money type: use BigInt (minor units / santim) for all monetary values.
 * NEVER use number/float for money.
 */
export type Santim = bigint;

/** Convert ETB display string to santim. E.g. "1500.00" → 150000n */
export function etbToSantim(etb: string): Santim {
  const [whole, fraction = '00'] = etb.split('.');
  const paddedFraction = fraction.padEnd(2, '0').slice(0, 2);
  return BigInt(whole) * 100n + BigInt(paddedFraction);
}

/** Convert santim to ETB display string. E.g. 150000n → "1,500.00" */
export function santimToEtb(santim: Santim): string {
  const abs = santim < 0n ? -santim : santim;
  const whole = abs / 100n;
  const frac  = abs % 100n;
  const sign  = santim < 0n ? '-' : '';
  return `${sign}${whole.toLocaleString('en-US')}.${String(frac).padStart(2, '0')}`;
}

/** Format for display: "ETB 1,500.00" */
export function formatEtb(santim: Santim): string {
  return `ETB ${santimToEtb(santim)}`;
}
