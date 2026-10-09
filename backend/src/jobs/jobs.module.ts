import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { JobsController } from './jobs.controller';
import { JobsService } from './jobs.service';
import { LedgerReconciliationJob } from './ledger-reconciliation.job';
import { VerifyEtReconciliationJob } from './verify-et-reconciliation.job';
import { LedgerEntry } from '../ledger/entities/ledger-entry.entity';
import { PaymentAttempt } from '../payments/entities/payment-attempt.entity';
import { VerificationModule } from '../verification/verification.module';
import { LedgerModule } from '../ledger/ledger.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([LedgerEntry, PaymentAttempt]),
    VerificationModule,
    LedgerModule,
  ],
  controllers: [JobsController],
  providers: [JobsService, LedgerReconciliationJob, VerifyEtReconciliationJob],
  exports: [JobsService, LedgerReconciliationJob, VerifyEtReconciliationJob],
})
export class JobsModule {}
