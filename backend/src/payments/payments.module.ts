import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PaymentsController } from './payments.controller';
import { PaymentsService } from './payments.service';
import { PaymentAttempt } from './entities/payment-attempt.entity';
import { OrgReceivingAccount } from './entities/org-receiving-account.entity';
import { VerificationModule } from '../verification/verification.module';
import { LedgerModule } from '../ledger/ledger.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([PaymentAttempt, OrgReceivingAccount]),
    VerificationModule,
    LedgerModule,
  ],
  controllers: [PaymentsController],
  providers: [PaymentsService],
  exports: [PaymentsService],
})
export class PaymentsModule {}
