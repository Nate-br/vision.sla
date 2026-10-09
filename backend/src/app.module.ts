import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ThrottlerModule } from '@nestjs/throttler';
import { ScheduleModule } from '@nestjs/schedule';
import { LoggerModule } from 'nestjs-pino';

import { AuthModule } from './auth/auth.module';
import { MembersModule } from './members/members.module';
import { PaymentsModule } from './payments/payments.module';
import { VerificationModule } from './verification/verification.module';
import { WebhooksModule } from './webhooks/webhooks.module';
import { LoansModule } from './loans/loans.module';
import { SavingsModule } from './savings/savings.module';
import { NotificationsModule } from './notifications/notifications.module';
import { LedgerModule } from './ledger/ledger.module';
import { StaffModule } from './staff/staff.module';
import { JobsModule } from './jobs/jobs.module';
import { StatementsModule } from './statements/statements.module';

@Module({
  imports: [
    // ─── Config ─────────────────────────────────────────────────────────────
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
      // Never expose secrets through config endpoints
      cache: true,
    }),

    // ─── Structured logging (pino) ────────────────────────────────────────
    LoggerModule.forRoot({
      pinoHttp: {
        level: process.env.NODE_ENV === 'production' ? 'info' : 'debug',
        redact: [
          // Scrub sensitive fields from logs — MASVS-RESILIENCE-2 equivalent
          'req.headers.authorization',
          'req.headers["x-api-key"]',
          'body.pin',
          'body.otp',
          'body.password',
          'body.reference',       // payment references are sensitive
          'body.transactionNumber',
        ],
      },
    }),

    // ─── Database ────────────────────────────────────────────────────────
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        type: 'postgres',
        host: config.get('DATABASE_HOST'),
        port: config.get<number>('DATABASE_PORT'),
        database: config.get('DATABASE_NAME'),
        username: config.get('DATABASE_USER'),
        password: config.get('DATABASE_PASSWORD'),
        ssl: config.get('DATABASE_SSL') === 'true',
        autoLoadEntities: true,
        // Migrations run as a separate step before deploy — never synchronize in prod
        synchronize: config.get('NODE_ENV') === 'development',
        logging: config.get('NODE_ENV') === 'development',
        extra: {
          // Pool tuned for 2 GB server (postgres max_connections=30)
          max: 10,
          idleTimeoutMillis: 30000,
          connectionTimeoutMillis: 5000,
        },
      }),
    }),

    // ─── Rate limiting ────────────────────────────────────────────────────
    ThrottlerModule.forRoot([
      { name: 'short', ttl: 1_000,  limit: 10 },
      { name: 'medium', ttl: 60_000, limit: 100 },
    ]),

    // ─── Cron jobs (reconciliation, cleanup) ─────────────────────────────
    ScheduleModule.forRoot(),

    // ─── Feature modules ─────────────────────────────────────────────────
    AuthModule,
    MembersModule,
    PaymentsModule,
    VerificationModule,
    WebhooksModule,
    LoansModule,
    SavingsModule,
    NotificationsModule,
    LedgerModule,
    StaffModule,
    JobsModule,
    StatementsModule,
  ],
})
export class AppModule {}
