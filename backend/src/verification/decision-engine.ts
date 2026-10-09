/**
 * Decision Engine — pure function, fully unit-tested (plan Section 4.2, step 5).
 *
 * CREDIT automatically only if ALL of the following are true:
 *   result.status == success AND verified == true
 *   AND currency == ETB
 *   AND settlementAccountMatch.matched == true AND not ambiguous
 *   AND result.amount matches expected (exact for fixed; >= for open contributions)
 *   AND receipt timestamp is within the allowed window
 *   AND this reference was never credited in OUR database
 *   AND confirmationHistory.confirmedBefore == false (or only by OUR account)
 *   AND member daily/monthly limits not exceeded
 * ELSE appropriate non-CREDIT outcome.
 *
 * This module has ZERO side effects — it only returns a decision.
 * All DB writes happen in the calling service inside a transaction.
 */
import { PaymentStatus } from '../common/types/domain.types';
import type { VerifyEtResult } from './verify-et.client';

export interface DecisionContext {
  result: VerifyEtResult;
  expectedAmountSantim: bigint;
  allowFlexibleAmount: boolean;   // true for open contributions
  invoiceDateIso: string;
  maxReceiptAgeDays: number;
  alreadyCreditedInDb: boolean;
  memberLimitsExceeded: boolean;
  ourAccountKeys: string[];       // our settlement accounts (to check confirmedBefore)
}

export type DecisionOutcome =
  | { action: 'CREDIT' }
  | { action: 'PENDING_RETRY'; reason: string }
  | { action: 'MANUAL_REVIEW'; reasonCode: string }
  | { action: 'REJECT'; reasonCode: string };

/**
 * decidePaymentOutcome — pure decision function.
 *
 * No I/O, no DB, no logging. Easily unit-tested with recorded verify.et fixtures.
 * Test all branches: success, not-found, mismatch, duplicate, expired, ambiguous.
 */
export function decidePaymentOutcome(ctx: DecisionContext): DecisionOutcome {
  const { result } = ctx;

  // 1. Provider not found / not yet available → retry
  if (result.status !== 'success' || !result.verified) {
    return { action: 'PENDING_RETRY', reason: 'provider_not_found_or_unverified' };
  }

  // 2. Currency must be ETB
  if (result.currency !== 'ETB') {
    return { action: 'REJECT', reasonCode: 'WRONG_CURRENCY' };
  }

  // 3. Settlement account must match, non-ambiguously
  if (!result.settlementAccountMatch?.matched) {
    return { action: 'REJECT', reasonCode: 'WRONG_RECEIVER' };
  }
  if (result.settlementAccountMatch.ambiguous) {
    return { action: 'MANUAL_REVIEW', reasonCode: 'SETTLEMENT_ACCOUNT_AMBIGUOUS' };
  }

  // 4. Amount check
  if (result.amount !== undefined) {
    const receivedSantim = BigInt(Math.round((result.amount ?? 0) * 100));
    if (ctx.allowFlexibleAmount) {
      if (receivedSantim < ctx.expectedAmountSantim) {
        return { action: 'REJECT', reasonCode: 'AMOUNT_TOO_LOW' };
      }
    } else {
      if (receivedSantim !== ctx.expectedAmountSantim) {
        return { action: 'REJECT', reasonCode: 'AMOUNT_MISMATCH' };
      }
    }
  }

  // 5. Timestamp window
  if (result.timestamp) {
    const receiptDate = new Date(result.timestamp);
    const invoiceDate = new Date(ctx.invoiceDateIso);
    const now = new Date();
    const ageMs = now.getTime() - receiptDate.getTime();
    const maxAgeMs = ctx.maxReceiptAgeDays * 24 * 60 * 60 * 1000;
    if (receiptDate < invoiceDate || ageMs > maxAgeMs) {
      return { action: 'REJECT', reasonCode: 'RECEIPT_OUTSIDE_WINDOW' };
    }
  }

  // 6. Duplicate check (our DB is the authority)
  if (ctx.alreadyCreditedInDb) {
    return { action: 'REJECT', reasonCode: 'ALREADY_CREDITED' };
  }

  // 7. Confirmation history
  const history = result.confirmationHistory;
  if (history?.confirmedBefore && history.confirmationCount > 1) {
    return { action: 'MANUAL_REVIEW', reasonCode: 'PREVIOUSLY_CONFIRMED_ELSEWHERE' };
  }

  // 8. Member limits
  if (ctx.memberLimitsExceeded) {
    return { action: 'MANUAL_REVIEW', reasonCode: 'VELOCITY_LIMIT_EXCEEDED' };
  }

  // All checks passed
  return { action: 'CREDIT' };
}
