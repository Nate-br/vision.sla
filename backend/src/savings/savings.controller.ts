import {
  Controller,
  Get,
  Query,
  Req,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { SavingsService } from './savings.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@ApiTags('Savings')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('savings')
export class SavingsController {
  constructor(private readonly savingsService: SavingsService) {}

  @Get('summary')
  @ApiOperation({ summary: 'Get current member savings balance breakdown' })
  async getSavingsSummary(@Req() req: any) {
    return this.savingsService.getSummary(req.user.id);
  }

  @Get('transactions')
  @ApiOperation({ summary: 'Get paginated member savings and deposit transaction history' })
  @ApiQuery({ name: 'page', required: false, example: 1 })
  @ApiQuery({ name: 'limit', required: false, example: 20 })
  async getTransactions(
    @Req() req: any,
    @Query('page') page: string = '1',
    @Query('limit') limit: string = '20',
  ) {
    const pageNum = parseInt(page, 10) || 1;
    const limitNum = parseInt(limit, 10) || 20;
    return this.savingsService.getTransactions(req.user.id, pageNum, limitNum);
  }
}
