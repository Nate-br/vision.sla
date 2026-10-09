import {
  Controller,
  Post,
  Get,
  Body,
  Param,
  Req,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';
import { PaymentsService } from './payments.service';
import { SubmitPaymentDto } from './dto/submit-payment.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@ApiTags('Payments')
@Controller('payments')
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post('submit')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Submit a transaction reference code for verification and settlement' })
  @ApiResponse({ status: 200, description: 'Payment verification processed' })
  async submitPayment(@Req() req: any, @Body() dto: SubmitPaymentDto) {
    return this.paymentsService.submitPayment(req.user.id, dto);
  }

  @Get('status/:id')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Check real-time status of a payment verification attempt' })
  async getPaymentStatus(@Req() req: any, @Param('id') paymentId: string) {
    return this.paymentsService.getPaymentStatus(paymentId, req.user.id);
  }

  @Get('receiving-accounts')
  @ApiOperation({ summary: 'List organization receiving bank accounts and wallet numbers for all 11 providers' })
  async getReceivingAccounts() {
    return this.paymentsService.getReceivingAccounts();
  }
}
