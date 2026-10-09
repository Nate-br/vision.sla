import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { JobsController } from './jobs.controller';
import { JobsService } from './jobs.service';
import { LedgerReconciliationJob } from './ledger-reconciliation.job';
import { LedgerEntry } from '../ledger/entities/ledger-entry.entity';

@Module({
  imports: [TypeOrmModule.forFeature([LedgerEntry])],
  controllers: [JobsController],
  providers: [JobsService, LedgerReconciliationJob],
  exports: [JobsService, LedgerReconciliationJob],
})
export class JobsModule {}
