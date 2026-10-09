import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Loan, LoanStatus } from './entities/loan.entity';

@Injectable()
export class LoansService {
  constructor(
    @InjectRepository(Loan)
    private readonly loanRepo: Repository<Loan>,
  ) {}

  /**
   * Retrieves summary of active/pending loans for the member dashboard.
   */
  async getSummary(memberId: string) {
    const activeLoan = await this.loanRepo.findOne({
      where: {
        memberId,
        status: In([LoanStatus.ACTIVE, LoanStatus.APPROVED, LoanStatus.PENDING]),
      },
      order: { createdAt: 'DESC' },
    });

    if (!activeLoan) {
      return {
        hasActiveLoan: false,
        activeLoan: null,
      };
    }

    return {
      hasActiveLoan: true,
      activeLoan: {
        id: activeLoan.id,
        productName: activeLoan.productName,
        loanType: activeLoan.loanType,
        principalSantim: activeLoan.principalSantim.toString(),
        totalDueSantim: activeLoan.totalDueSantim.toString(),
        balanceRemainingSantim: activeLoan.balanceRemainingSantim.toString(),
        monthlyInstallmentSantim: activeLoan.monthlyInstallmentSantim.toString(),
        status: activeLoan.status,
        termMonths: activeLoan.termMonths,
        paidInstallments: activeLoan.paidInstallments,
        totalInstallments: activeLoan.totalInstallments,
        nextPaymentDueDate: activeLoan.nextPaymentDueDate,
      },
    };
  }

  /**
   * Retrieves list of all loans (history + active) for member.
   */
  async getAllLoans(memberId: string) {
    return this.loanRepo.find({
      where: { memberId },
      order: { createdAt: 'DESC' },
    });
  }
}
