import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Loan, LoanStatus } from './entities/loan.entity';
import { LoanProduct } from './entities/loan-product.entity';
import { LoanApplication, LoanApplicationStatus } from './entities/loan-application.entity';
import { LoanInstallment, InstallmentStatus } from './entities/loan-installment.entity';
import { ApplyLoanDto, CalculateLoanDto } from './dto/loan-operations.dto';

@Injectable()
export class LoansService {
  constructor(
    @InjectRepository(Loan)
    private readonly loanRepo: Repository<Loan>,
    @InjectRepository(LoanProduct)
    private readonly productRepo: Repository<LoanProduct>,
    @InjectRepository(LoanApplication)
    private readonly applicationRepo: Repository<LoanApplication>,
    @InjectRepository(LoanInstallment)
    private readonly installmentRepo: Repository<LoanInstallment>,
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

  /**
   * Retrieves available loan products.
   */
  async getProducts() {
    return this.productRepo.find({
      where: { isActive: true },
      order: { createdAt: 'ASC' },
    });
  }

  /**
   * Calculates monthly repayment, total interest, and amortization preview.
   */
  async calculateLoan(dto: CalculateLoanDto) {
    const product = await this.productRepo.findOne({
      where: { id: dto.productId, isActive: true },
    });
    if (!product) {
      throw new NotFoundException('Loan product not found or inactive');
    }

    const principal = BigInt(dto.amountSantim);
    const months = BigInt(dto.termMonths);

    // Interest calculation: Annual Rate (BPS) / 10000 / 12 * termMonths
    // Total Interest = Principal * (interestRateBps / 10000) * (termMonths / 12)
    // To maintain integer arithmetic without float drift:
    // Total Interest (santim) = (Principal * interestRateBps * termMonths) / (10000 * 12)
    const totalInterestSantim = (principal * BigInt(product.interestRateBps) * months) / (120000n);
    const totalDueSantim = principal + totalInterestSantim;
    const monthlyInstallmentSantim = totalDueSantim / months;

    // Processing fee = (Principal * processingFeeBps) / 10000
    const processingFeeSantim = (principal * BigInt(product.processingFeeBps)) / 10000n;

    // Generate schedule preview
    const schedulePreview = [];
    let remainingPrincipal = principal;
    const principalPerMonth = principal / months;
    const interestPerMonth = totalInterestSantim / months;

    for (let i = 1; i <= dto.termMonths; i++) {
      const isLast = i === dto.termMonths;
      const prinPortion = isLast ? remainingPrincipal : principalPerMonth;
      remainingPrincipal -= prinPortion;

      schedulePreview.push({
        installmentNumber: i,
        principalSantim: prinPortion.toString(),
        interestSantim: interestPerMonth.toString(),
        totalSantim: (prinPortion + interestPerMonth).toString(),
      });
    }

    return {
      productId: product.id,
      productName: product.name,
      principalSantim: principal.toString(),
      interestRateBps: product.interestRateBps,
      termMonths: dto.termMonths,
      totalInterestSantim: totalInterestSantim.toString(),
      totalDueSantim: totalDueSantim.toString(),
      monthlyInstallmentSantim: monthlyInstallmentSantim.toString(),
      processingFeeSantim: processingFeeSantim.toString(),
      schedulePreview,
    };
  }

  /**
   * Applies for a loan.
   */
  async applyForLoan(memberId: string, dto: ApplyLoanDto) {
    const product = await this.productRepo.findOne({
      where: { id: dto.productId, isActive: true },
    });
    if (!product) {
      throw new NotFoundException('Loan product not found or inactive');
    }

    const requestedAmount = BigInt(dto.requestedAmountSantim);
    const minAmount = BigInt(product.minAmountSantim);
    const maxAmount = BigInt(product.maxAmountSantim);

    if (requestedAmount < minAmount || requestedAmount > maxAmount) {
      throw new BadRequestException(
        `Requested amount must be between ${minAmount} and ${maxAmount} santim`,
      );
    }

    if (dto.termMonths < product.minTenureMonths || dto.termMonths > product.maxTenureMonths) {
      throw new BadRequestException(
        `Tenure must be between ${product.minTenureMonths} and ${product.maxTenureMonths} months`,
      );
    }

    const application = this.applicationRepo.create({
      memberId,
      productId: product.id,
      requestedAmountSantim: dto.requestedAmountSantim,
      termMonths: dto.termMonths,
      purpose: dto.purpose,
      status: LoanApplicationStatus.SUBMITTED,
    });

    return this.applicationRepo.save(application);
  }

  /**
   * Lists loan applications for member.
   */
  async getApplications(memberId: string) {
    return this.applicationRepo.find({
      where: { memberId },
      order: { createdAt: 'DESC' },
    });
  }

  /**
   * Retrieves installment schedule for a loan.
   */
  async getLoanSchedule(memberId: string, loanId: string) {
    const loan = await this.loanRepo.findOne({
      where: { id: loanId, memberId },
    });
    if (!loan) {
      throw new NotFoundException('Loan not found');
    }

    const installments = await this.installmentRepo.find({
      where: { loanId },
      order: { installmentNumber: 'ASC' },
    });

    return {
      loanId: loan.id,
      productName: loan.productName,
      status: loan.status,
      totalDueSantim: loan.totalDueSantim.toString(),
      balanceRemainingSantim: loan.balanceRemainingSantim.toString(),
      paidInstallments: loan.paidInstallments,
      totalInstallments: loan.totalInstallments,
      installments,
    };
  }

  /**
   * Waterfall Repayment Auto-Allocation Engine (S-401).
   *
   * Sequentially allocates payment against unpaid installments:
   * 1. Sort unpaid installments by dueDate ASC, installmentNumber ASC.
   * 2. For each installment: satisfy interest due first, then principal due.
   * 3. Update installment status (PAID or PARTIALLY_PAID).
   * 4. Updates Loan balanceRemainingSantim and status.
   * 5. Excess overpayment is returned for voluntary savings credit (S-403).
   */
  async allocateRepayment(loanId: string, paymentAmountSantim: string, memberId: string) {
    const loan = await this.loanRepo.findOne({
      where: { id: loanId, memberId },
    });
    if (!loan) {
      throw new NotFoundException('Loan not found');
    }

    let remainingPayment = BigInt(paymentAmountSantim);
    const initialPayment = remainingPayment;

    const unpaidInstallments = await this.installmentRepo.find({
      where: {
        loanId,
        status: In([InstallmentStatus.PENDING, InstallmentStatus.PARTIALLY_PAID, InstallmentStatus.OVERDUE]),
      },
      order: { installmentNumber: 'ASC' },
    });

    const updatedInstallments: LoanInstallment[] = [];

    for (const inst of unpaidInstallments) {
      if (remainingPayment <= 0n) break;

      const interestDue = BigInt(inst.interestDueSantim);
      const interestPaid = BigInt(inst.interestPaidSantim);
      const unpaidInterest = interestDue > interestPaid ? interestDue - interestPaid : 0n;

      // 1. Pay Interest First
      const interestToPay = remainingPayment >= unpaidInterest ? unpaidInterest : remainingPayment;
      const newInterestPaid = interestPaid + interestToPay;
      remainingPayment -= interestToPay;

      // 2. Pay Principal Next
      const principalDue = BigInt(inst.principalDueSantim);
      const principalPaid = BigInt(inst.principalPaidSantim);
      const unpaidPrincipal = principalDue > principalPaid ? principalDue - principalPaid : 0n;

      const principalToPay = remainingPayment >= unpaidPrincipal ? unpaidPrincipal : remainingPayment;
      const newPrincipalPaid = principalPaid + principalToPay;
      remainingPayment -= principalToPay;

      const totalPaidForInst = newInterestPaid + newPrincipalPaid;
      const totalDueForInst = interestDue + principalDue;

      inst.interestPaidSantim = newInterestPaid.toString();
      inst.principalPaidSantim = newPrincipalPaid.toString();
      inst.totalPaidSantim = totalPaidForInst.toString();

      if (totalPaidForInst >= totalDueForInst) {
        inst.status = InstallmentStatus.PAID;
        inst.paidAt = new Date();
      } else {
        inst.status = InstallmentStatus.PARTIALLY_PAID;
      }

      await this.installmentRepo.save(inst);
      updatedInstallments.push(inst);
    }

    // Update Loan balance and installment counts
    const allocated = initialPayment - remainingPayment;
    const currentBalance = BigInt(loan.balanceRemainingSantim);
    const newBalance = currentBalance > allocated ? currentBalance - allocated : 0n;
    loan.balanceRemainingSantim = newBalance.toString();

    const fullyPaidCount = await this.installmentRepo.count({
      where: { loanId, status: InstallmentStatus.PAID },
    });
    loan.paidInstallments = fullyPaidCount;

    if (newBalance === 0n || loan.paidInstallments >= loan.totalInstallments) {
      loan.status = LoanStatus.PAID_OFF;
      loan.nextPaymentDueDate = null;
    } else {
      // Find next pending installment due date
      const nextPending = await this.installmentRepo.findOne({
        where: {
          loanId,
          status: In([InstallmentStatus.PENDING, InstallmentStatus.PARTIALLY_PAID, InstallmentStatus.OVERDUE]),
        },
        order: { installmentNumber: 'ASC' },
      });
      loan.nextPaymentDueDate = nextPending ? nextPending.dueDate : null;
    }

    await this.loanRepo.save(loan);

    return {
      loanId: loan.id,
      allocatedSantim: allocated.toString(),
      remainingOverpaymentSantim: remainingPayment.toString(),
      newBalanceRemainingSantim: loan.balanceRemainingSantim,
      loanStatus: loan.status,
      paidInstallments: loan.paidInstallments,
      updatedInstallmentsCount: updatedInstallments.length,
    };
  }
}
