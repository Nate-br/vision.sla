import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SavingsController } from './savings.controller';
import { SavingsService } from './savings.service';
import { SavingsAccount } from './entities/savings-account.entity';
import { SavingsTransaction } from './entities/savings-transaction.entity';

@Module({
  imports: [TypeOrmModule.forFeature([SavingsAccount, SavingsTransaction])],
  controllers: [SavingsController],
  providers: [SavingsService],
  exports: [SavingsService],
})
export class SavingsModule {}
