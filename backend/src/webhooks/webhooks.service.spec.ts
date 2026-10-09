import { WebhooksService, MAX_TIMESTAMP_TOLERANCE_MS } from './webhooks.service';
import { WebhookEvent } from './entities/webhook-event.entity';
import { PaymentAttempt } from '../payments/entities/payment-attempt.entity';
import { LedgerService } from '../ledger/ledger.service';
import { ConfigService } from '@nestjs/config';
import { Repository } from 'typeorm';
import * as crypto from 'crypto';
import { UnauthorizedException, BadRequestException } from '@nestjs/common';

describe('WebhooksService', () => {
  let service: WebhooksService;
  let webhookRepo: Partial<Repository<WebhookEvent>>;
  let paymentRepo: Partial<Repository<PaymentAttempt>>;
  let ledgerService: Partial<LedgerService>;
  const testSecret = 'whsec_test_secret_key_123';

  beforeEach(() => {
    webhookRepo = {
      findOne: jest.fn(),
      create: jest.fn().mockImplementation((e) => ({ id: 'evt-1', ...e })),
      save: jest.fn().mockImplementation((e) => Promise.resolve(e)),
    };

    paymentRepo = {
      findOne: jest.fn(),
      save: jest.fn().mockImplementation((p) => Promise.resolve(p)),
    };

    ledgerService = {
      postDoubleEntry: jest.fn().mockResolvedValue(undefined),
    };

    const configService = {
      get: jest.fn().mockReturnValue(testSecret),
    } as any;

    service = new WebhooksService(
      configService,
      webhookRepo as Repository<WebhookEvent>,
      paymentRepo as Repository<PaymentAttempt>,
      ledgerService as LedgerService,
    );
  });

  const computeSignature = (body: string): string => {
    return crypto.createHmac('sha256', testSecret).update(body).digest('hex');
  };

  it('should verify valid HMAC signature (S-304)', () => {
    const body = JSON.stringify({ event: 'verification.completed', data: { id: 1 } });
    const sig = computeSignature(body);

    expect(service.verifySignature(body, sig)).toBe(true);
    expect(service.verifySignature(body, `t=169000,v1=${sig}`)).toBe(true);
  });

  it('should reject invalid or forged HMAC signature (S-304)', () => {
    const body = JSON.stringify({ event: 'verification.completed' });
    expect(service.verifySignature(body, 'forged_signature_hex')).toBe(false);
  });

  it('should enforce timestamp tolerance <= 5 minutes (S-305)', () => {
    const nowSec = Math.floor(Date.now() / 1000).toString();
    expect(service.validateTimestamp(nowSec)).toBe(true);

    const oldSec = Math.floor((Date.now() - 10 * 60 * 1000) / 1000).toString(); // 10 minutes ago
    expect(service.validateTimestamp(oldSec)).toBe(false);
  });

  it('should reject forged webhook requests with 401 Unauthorized', async () => {
    const rawBody = '{"event":"test"}';
    await expect(
      service.handleWebhook(rawBody, 'wrong_sig', undefined, 'evt-test', {}),
    ).rejects.toThrow(UnauthorizedException);
  });

  it('should deduplicate already processed event IDs idempotently (S-306)', async () => {
    const rawBody = '{"event":"test"}';
    const sig = computeSignature(rawBody);

    (webhookRepo.findOne as jest.Mock).mockResolvedValue({
      id: 'existing-id',
      eventId: 'evt-dup',
      isProcessed: true,
    } as WebhookEvent);

    const result = await service.handleWebhook(
      rawBody,
      sig,
      Math.floor(Date.now() / 1000).toString(),
      'evt-dup',
      {},
    );

    expect(result.status).toBe('ALREADY_PROCESSED');
    expect(paymentRepo.save).not.toHaveBeenCalled();
  });
});
