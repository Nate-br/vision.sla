import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { StatementsController } from './statements.controller';
import { StatementsService } from './statements.service';
import { SavingsAccount } from '../savings/entities/savings-account.entity';
import { SavingsTransaction } from '../savings/entities/savings-transaction.entity';
import { Loan } from '../loans/entities/loan.entity';
import { CustomerProfile } from '../members/entities/customer-profile.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      SavingsAccount,
      SavingsTransaction,
      Loan,
      CustomerProfile,
    ]),
  ],
  controllers: [StatementsController],
  providers: [StatementsService],
  exports: [StatementsService],
})
export class StatementsModule {}
