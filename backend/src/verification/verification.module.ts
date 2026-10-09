import { Module } from '@nestjs/common';
import { VerificationController } from './verification.controller';
import { VerificationService } from './verification.service';
import { VerifyEtClient } from './verify-et.client';

@Module({
  controllers: [VerificationController],
  providers: [VerificationService, VerifyEtClient],
  exports: [VerificationService, VerifyEtClient],
})
export class VerificationModule {}
