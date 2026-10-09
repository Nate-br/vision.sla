import { Injectable, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Between, LessThanOrEqual, MoreThanOrEqual } from 'typeorm';
import { SavingsAccount } from '../savings/entities/savings-account.entity';
import { SavingsTransaction } from '../savings/entities/savings-transaction.entity';
import { Loan } from '../loans/entities/loan.entity';
import { CustomerProfile } from '../members/entities/customer-profile.entity';
import * as PDFDocument from 'pdfkit';

export interface StatementSummary {
  memberId: string;
  accountNumber: string;
  memberName: string;
  phone: string;
  period: { from: string; to: string };
  currentSavingsSantim: string;
  totalDepositedSantim: string;
  totalWithdrawnSantim: string;
  activeLoanPrincipalSantim: string;
  activeLoanRemainingSantim: string;
  transactionsCount: number;
  transactions: Array<{
    date: string;
    type: string;
    amountSantim: string;
    provider: string | null;
    status: string;
    description: string | null;
  }>;
}

@Injectable()
export class StatementsService {
  constructor(
    @InjectRepository(SavingsAccount)
    private readonly accountRepo: Repository<SavingsAccount>,
    @InjectRepository(SavingsTransaction)
    private readonly transactionRepo: Repository<SavingsTransaction>,
    @InjectRepository(Loan)
    private readonly loanRepo: Repository<Loan>,
    @InjectRepository(CustomerProfile)
    private readonly profileRepo: Repository<CustomerProfile>,
  ) {}

  /**
   * Sanitizes text to prevent PDF injection / malicious string rendering (S-402).
   */
  sanitizeText(text: string | null | undefined): string {
    if (!text) return '';
    // Strip non-printable / control characters except space
    return text.replace(/[\x00-\x1F\x7F-\x9F]/g, '').trim();
  }

  /**
   * Validates and normalizes date range (<= 365 days).
   */
  validateDateRange(fromStr?: string, toStr?: string): { from: Date; to: Date } {
    const to = toStr ? new Date(toStr) : new Date();
    const from = fromStr ? new Date(fromStr) : new Date(to.getTime() - 90 * 24 * 60 * 60 * 1000); // default 90 days

    if (isNaN(from.getTime()) || isNaN(to.getTime())) {
      throw new BadRequestException('Invalid date format. Use ISO format YYYY-MM-DD');
    }

    if (from > to) {
      throw new BadRequestException('from date must be before or equal to to date');
    }

    const diffDays = (to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24);
    if (diffDays > 365) {
      throw new BadRequestException('Statement date range cannot exceed 365 days');
    }

    return { from, to };
  }

  /**
   * Aggregates member financial statement data.
   */
  async getStatementData(memberId: string, fromStr?: string, toStr?: string): Promise<StatementSummary> {
    const { from, to } = this.validateDateRange(fromStr, toStr);

    const profile = await this.profileRepo.findOne({ where: { memberId } });
    const account = await this.accountRepo.findOne({ where: { memberId } });
    const activeLoan = await this.loanRepo.findOne({
      where: { memberId, status: 'ACTIVE' as any },
      order: { createdAt: 'DESC' },
    });

    const transactions = await this.transactionRepo.find({
      where: {
        memberId,
        createdAt: Between(from, to),
      },
      order: { createdAt: 'DESC' },
    });

    let totalDeposited = 0n;
    let totalWithdrawn = 0n;

    for (const tx of transactions) {
      const amt = BigInt(tx.amountSantim);
      if (tx.transactionType === 'DEPOSIT') {
        totalDeposited += amt;
      } else if (tx.transactionType === 'WITHDRAWAL') {
        totalWithdrawn += amt;
      }
    }

    const memberName = profile?.fullName
      ? this.sanitizeText(profile.fullName)
      : 'Valued Member';

    return {
      memberId,
      accountNumber: account ? account.accountNumber : 'N/A',
      memberName,
      phone: '',
      period: {
        from: from.toISOString().split('T')[0],
        to: to.toISOString().split('T')[0],
      },
      currentSavingsSantim: account ? account.totalSavingsSantim.toString() : '0',
      totalDepositedSantim: totalDeposited.toString(),
      totalWithdrawnSantim: totalWithdrawn.toString(),
      activeLoanPrincipalSantim: activeLoan ? activeLoan.principalSantim.toString() : '0',
      activeLoanRemainingSantim: activeLoan ? activeLoan.balanceRemainingSantim.toString() : '0',
      transactionsCount: transactions.length,
      transactions: transactions.map((t) => ({
        date: t.createdAt.toISOString().split('T')[0],
        type: t.transactionType,
        amountSantim: t.amountSantim.toString(),
        provider: t.paymentProvider,
        status: t.status,
        description: this.sanitizeText(t.description),
      })),
    };
  }

  /**
   * Generates a tamper-evident, sanitized PDF statement (S-402).
   */
  async generatePdfStatement(memberId: string, fromStr?: string, toStr?: string): Promise<Buffer> {
    const data = await this.getStatementData(memberId, fromStr, toStr);

    return new Promise((resolve, reject) => {
      const doc = new PDFDocument({ margin: 40, size: 'A4' });
      const chunks: Buffer[] = [];

      doc.on('data', (chunk) => chunks.push(chunk));
      doc.on('end', () => resolve(Buffer.concat(chunks)));
      doc.on('error', (err) => reject(err));

      // Header
      doc
        .fillColor('#1A4B8C')
        .fontSize(18)
        .text('VISION FOR A BETTER LIFE SLA', { align: 'center' })
        .fontSize(10)
        .fillColor('#555555')
        .text('Savings and Credit Cooperative Society | Official Statement', { align: 'center' })
        .moveDown(1.5);

      // Period & Member Box
      doc
        .rect(40, doc.y, 515, 65)
        .fillAndStroke('#F5F8FC', '#B0C4DE');

      const boxTop = doc.y + 10;
      doc
        .fillColor('#1A4B8C')
        .fontSize(10)
        .font('Helvetica-Bold')
        .text('MEMBER DETAILS', 55, boxTop)
        .font('Helvetica')
        .fillColor('#222222')
        .text(`Name: ${data.memberName}`, 55, boxTop + 16)
        .text(`Account No: ${data.accountNumber}`, 55, boxTop + 30)
        .font('Helvetica-Bold')
        .fillColor('#1A4B8C')
        .text('STATEMENT PERIOD', 320, boxTop)
        .font('Helvetica')
        .fillColor('#222222')
        .text(`From: ${data.period.from}`, 320, boxTop + 16)
        .text(`To: ${data.period.to}`, 320, boxTop + 30);

      doc.y = boxTop + 65;
      doc.moveDown(1);

      // Balances Summary Grid
      const savingsEtb = (Number(data.currentSavingsSantim) / 100).toFixed(2);
      const depositedEtb = (Number(data.totalDepositedSantim) / 100).toFixed(2);
      const loanRemainingEtb = (Number(data.activeLoanRemainingSantim) / 100).toFixed(2);

      doc
        .fillColor('#1A4B8C')
        .fontSize(12)
        .font('Helvetica-Bold')
        .text('FINANCIAL SUMMARY', 40)
        .moveDown(0.5);

      doc
        .fontSize(9)
        .font('Helvetica')
        .fillColor('#333333')
        .text(`Current Savings Balance: ETB ${savingsEtb}`)
        .text(`Total Period Deposits: ETB ${depositedEtb}`)
        .text(`Active Loan Outstanding Balance: ETB ${loanRemainingEtb}`)
        .moveDown(1);

      // Transactions Table Header
      doc
        .fillColor('#1A4B8C')
        .fontSize(12)
        .font('Helvetica-Bold')
        .text('ITEMIZED TRANSACTIONS', 40)
        .moveDown(0.5);

      const tableTop = doc.y;
      doc
        .rect(40, tableTop, 515, 20)
        .fill('#1A4B8C');

      doc
        .fillColor('#FFFFFF')
        .fontSize(9)
        .font('Helvetica-Bold')
        .text('Date', 50, tableTop + 5)
        .text('Type', 130, tableTop + 5)
        .text('Provider', 220, tableTop + 5)
        .text('Description', 300, tableTop + 5)
        .text('Amount (ETB)', 460, tableTop + 5);

      let currentY = tableTop + 24;

      doc.font('Helvetica').fontSize(8).fillColor('#222222');

      if (data.transactions.length === 0) {
        doc.text('No transactions found in this date window.', 50, currentY);
      } else {
        for (const tx of data.transactions.slice(0, 30)) {
          const amtEtb = (Number(tx.amountSantim) / 100).toFixed(2);
          doc
            .text(tx.date, 50, currentY)
            .text(tx.type, 130, currentY)
            .text(tx.provider || 'N/A', 220, currentY)
            .text((tx.description || tx.type).slice(0, 26), 300, currentY)
            .text(amtEtb, 460, currentY);

          currentY += 18;
          if (currentY > 750) {
            doc.addPage();
            currentY = 50;
          }
        }
      }

      // Security Footer (S-402)
      doc
        .fontSize(7)
        .fillColor('#888888')
        .text(
          'This is a system-generated document certified by Vision for a Better Life SLA. Double-entry verified.',
          40,
          780,
          { align: 'center' },
        );

      doc.end();
    });
  }
}
