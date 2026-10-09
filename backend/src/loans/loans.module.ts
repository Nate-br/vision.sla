import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { LoansController } from './loans.controller';
import { LoansService } from './loans.service';
import { Loan } from './entities/loan.entity';
import { LoanProduct } from './entities/loan-product.entity';
import { LoanApplication } from './entities/loan-application.entity';
import { LoanInstallment } from './entities/loan-installment.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Loan, LoanProduct, LoanApplication, LoanInstallment])],
  controllers: [LoansController],
  providers: [LoansService],
  exports: [LoansService],
})
export class LoansModule {}
