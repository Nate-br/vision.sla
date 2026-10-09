import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule, TypeOrmModuleOptions } from '@nestjs/typeorm';
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
      useFactory: (config: ConfigService): TypeOrmModuleOptions => {
        const dbType = (config.get('DATABASE_TYPE') || 'postgres') as any;
        const isMysqlOrMaria = dbType === 'mariadb' || dbType === 'mysql';
        return {
          type: isMysqlOrMaria ? 'mariadb' : 'postgres',
          host: config.get<string>('DATABASE_HOST', 'localhost'),
          port: config.get<number>('DATABASE_PORT', isMysqlOrMaria ? 3306 : 5432),
          database: config.get<string>('DATABASE_NAME'),
          username: config.get<string>('DATABASE_USER'),
          password: config.get<string>('DATABASE_PASSWORD'),
          extra: config.get('DATABASE_SOCKET')
            ? { socketPath: config.get('DATABASE_SOCKET') }
            : isMysqlOrMaria
              ? { connectionLimit: 10 }
              : { max: 10, idleTimeoutMillis: 30000, connectionTimeoutMillis: 5000 },
          ssl: config.get('DATABASE_SSL') === 'true',
          autoLoadEntities: true,
          synchronize: config.get('NODE_ENV') === 'development' || config.get('DATABASE_SYNCHRONIZE') === 'true',
          logging: config.get('NODE_ENV') === 'development',
        };
      },
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
