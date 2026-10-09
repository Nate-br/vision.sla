import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Req,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { LoansService } from './loans.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ApplyLoanDto, CalculateLoanDto } from './dto/loan-operations.dto';

@ApiTags('Loans')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('loans')
export class LoansController {
  constructor(private readonly loansService: LoansService) {}

  @Get('products')
  @ApiOperation({ summary: 'List all available credit loan products' })
  async getProducts() {
    return this.loansService.getProducts();
  }

  @Post('calculate')
  @ApiOperation({ summary: 'Calculate repayment terms and amortization schedule' })
  async calculateLoan(@Body() dto: CalculateLoanDto) {
    return this.loansService.calculateLoan(dto);
  }

  @Post('apply')
  @ApiOperation({ summary: 'Submit new loan application' })
  async applyForLoan(@Req() req: any, @Body() dto: ApplyLoanDto) {
    return this.loansService.applyForLoan(req.user.id, dto);
  }

  @Get('applications')
  @ApiOperation({ summary: 'List loan applications for current member' })
  async getApplications(@Req() req: any) {
    return this.loansService.getApplications(req.user.id);
  }

  @Get('summary')
  @ApiOperation({ summary: 'Get active loan summary and next installment due details' })
  async getLoanSummary(@Req() req: any) {
    return this.loansService.getSummary(req.user.id);
  }

  @Get(':id/schedule')
  @ApiOperation({ summary: 'Get detailed repayment schedule and installments for a loan' })
  async getLoanSchedule(@Req() req: any, @Param('id') loanId: string) {
    return this.loansService.getLoanSchedule(req.user.id, loanId);
  }

  @Get()
  @ApiOperation({ summary: 'List all loans for the member' })
  async getAllLoans(@Req() req: any) {
    return this.loansService.getAllLoans(req.user.id);
  }
}
