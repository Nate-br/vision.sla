import {
  Controller,
  Get,
  Query,
  Req,
  Res,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { StatementsService } from './statements.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { FastifyReply } from 'fastify';

@ApiTags('Statements')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('statements')
export class StatementsController {
  constructor(private readonly statementsService: StatementsService) {}

  @Get()
  @ApiOperation({ summary: 'Get statement data breakdown and transaction history' })
  @ApiQuery({ name: 'from', required: false, description: 'Start date (YYYY-MM-DD)' })
  @ApiQuery({ name: 'to', required: false, description: 'End date (YYYY-MM-DD)' })
  async getStatement(
    @Req() req: any,
    @Query('from') from?: string,
    @Query('to') to?: string,
  ) {
    return this.statementsService.getStatementData(req.user.id, from, to);
  }

  @Get('pdf')
  @ApiOperation({ summary: 'Export official statement as PDF document' })
  @ApiQuery({ name: 'from', required: false })
  @ApiQuery({ name: 'to', required: false })
  async exportPdfStatement(
    @Req() req: any,
    @Res() res: FastifyReply,
    @Query('from') from?: string,
    @Query('to') to?: string,
  ) {
    const pdfBuffer = await this.statementsService.generatePdfStatement(req.user.id, from, to);

    const filename = `vbl-statement-${new Date().toISOString().split('T')[0]}.pdf`;

    res.header('Content-Type', 'application/pdf');
    res.header('Content-Disposition', `attachment; filename="${filename}"`);
    res.header('Content-Length', pdfBuffer.length.toString());

    return res.send(pdfBuffer);
  }
}
