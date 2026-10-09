/**
 * Decision Engine unit tests.
 *
 * Plan Section 4.2: "pure function, fully unit-tested"
 * Plan Section 13: "contract tests that replay recorded verify.et responses
 *   (success, not found, masked receiver, mismatch, 402, 429, 503)"
 *
 * ALL BRANCHES must be covered. Never hit the live verify.et API in tests.
 */
import { decidePaymentOutcome, DecisionContext } from './decision-engine';
import type { VerifyEtResult } from './verify-et.client';

const BASE_RESULT: VerifyEtResult = {
  requestId: 'req-001',
  status: 'success',
  verified: true,
  currency: 'ETB',
  amount: 500.00,
  timestamp: new Date().toISOString(),
  settlementAccountMatch: { matched: true, ambiguous: false },
  confirmationHistory: { confirmedBefore: false, confirmationCount: 0 },
};

const BASE_CTX: DecisionContext = {
  result: BASE_RESULT,
  expectedAmountSantim: 50000n,  // 500.00 ETB
  allowFlexibleAmount: false,
  invoiceDateIso: new Date(Date.now() - 60_000).toISOString(), // 1 min ago
  maxReceiptAgeDays: 30,
  alreadyCreditedInDb: false,
  memberLimitsExceeded: false,
  ourAccountKeys: ['1000123456789'],
};

describe('decidePaymentOutcome', () => {
  it('credits when all conditions are met', () => {
    expect(decidePaymentOutcome(BASE_CTX)).toEqual({ action: 'CREDIT' });
  });

  it('retries when provider not found (status != success)', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: { ...BASE_RESULT, status: 'not_found', verified: false },
    };
    const outcome = decidePaymentOutcome(ctx);
    expect(outcome.action).toBe('PENDING_RETRY');
  });

  it('rejects wrong currency', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: { ...BASE_RESULT, currency: 'USD' },
    };
    expect(decidePaymentOutcome(ctx)).toEqual({ action: 'REJECT', reasonCode: 'WRONG_CURRENCY' });
  });

  it('rejects when settlement account does not match', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: {
        ...BASE_RESULT,
        settlementAccountMatch: { matched: false },
      },
    };
    expect(decidePaymentOutcome(ctx)).toEqual({ action: 'REJECT', reasonCode: 'WRONG_RECEIVER' });
  });

  it('sends to manual review when settlement account is ambiguous', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: {
        ...BASE_RESULT,
        settlementAccountMatch: { matched: true, ambiguous: true },
      },
    };
    expect(decidePaymentOutcome(ctx)).toEqual({
      action: 'MANUAL_REVIEW',
      reasonCode: 'SETTLEMENT_ACCOUNT_AMBIGUOUS',
    });
  });

  it('rejects exact amount mismatch (fixed payment)', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: { ...BASE_RESULT, amount: 400.00 },
      expectedAmountSantim: 50000n,
      allowFlexibleAmount: false,
    };
    expect(decidePaymentOutcome(ctx)).toEqual({ action: 'REJECT', reasonCode: 'AMOUNT_MISMATCH' });
  });

  it('credits when amount >= expected for flexible contribution', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: { ...BASE_RESULT, amount: 600.00 },
      expectedAmountSantim: 50000n,
      allowFlexibleAmount: true,
    };
    expect(decidePaymentOutcome(ctx)).toEqual({ action: 'CREDIT' });
  });

  it('rejects flexible contribution when amount < minimum', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: { ...BASE_RESULT, amount: 400.00 },
      expectedAmountSantim: 50000n,
      allowFlexibleAmount: true,
    };
    expect(decidePaymentOutcome(ctx)).toEqual({ action: 'REJECT', reasonCode: 'AMOUNT_TOO_LOW' });
  });

  it('rejects receipt older than allowed window', () => {
    const oldTimestamp = new Date(Date.now() - 40 * 24 * 60 * 60 * 1000).toISOString();
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: { ...BASE_RESULT, timestamp: oldTimestamp },
      maxReceiptAgeDays: 30,
    };
    expect(decidePaymentOutcome(ctx)).toEqual({
      action: 'REJECT',
      reasonCode: 'RECEIPT_OUTSIDE_WINDOW',
    });
  });

  it('rejects if already credited in our DB', () => {
    const ctx: DecisionContext = { ...BASE_CTX, alreadyCreditedInDb: true };
    expect(decidePaymentOutcome(ctx)).toEqual({ action: 'REJECT', reasonCode: 'ALREADY_CREDITED' });
  });

  it('sends to manual review if previously confirmed elsewhere', () => {
    const ctx: DecisionContext = {
      ...BASE_CTX,
      result: {
        ...BASE_RESULT,
        confirmationHistory: { confirmedBefore: true, confirmationCount: 2 },
      },
    };
    expect(decidePaymentOutcome(ctx)).toEqual({
      action: 'MANUAL_REVIEW',
      reasonCode: 'PREVIOUSLY_CONFIRMED_ELSEWHERE',
    });
  });

  it('sends to manual review on velocity limit exceeded', () => {
    const ctx: DecisionContext = { ...BASE_CTX, memberLimitsExceeded: true };
    expect(decidePaymentOutcome(ctx)).toEqual({
      action: 'MANUAL_REVIEW',
      reasonCode: 'VELOCITY_LIMIT_EXCEEDED',
    });
  });
});
