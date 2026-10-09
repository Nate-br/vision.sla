import {
  Controller,
  Get,
  Req,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { LoansService } from './loans.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@ApiTags('Loans')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('loans')
export class LoansController {
  constructor(private readonly loansService: LoansService) {}

  @Get('summary')
  @ApiOperation({ summary: 'Get active loan summary and next installment due details' })
  async getLoanSummary(@Req() req: any) {
    return this.loansService.getSummary(req.user.id);
  }

  @Get()
  @ApiOperation({ summary: 'List all loans for the member' })
  async getAllLoans(@Req() req: any) {
    return this.loansService.getAllLoans(req.user.id);
  }
}
