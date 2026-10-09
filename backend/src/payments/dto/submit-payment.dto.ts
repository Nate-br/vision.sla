import { IsNotEmpty, IsString, IsEnum, IsOptional, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { PaymentProvider } from '../../common/types/domain.types';
import { PaymentPurpose } from '../entities/payment-attempt.entity';

export class SubmitPaymentDto {
  @ApiProperty({
    enum: PaymentProvider,
    example: PaymentProvider.TELEBIRR,
    description: 'One of the 11 supported Ethiopian payment providers',
  })
  @IsNotEmpty()
  @IsEnum(PaymentProvider)
  provider: PaymentProvider;

  @ApiProperty({
    example: 'RA47KL92X',
    description: 'Transaction confirmation reference code from SMS or receipt',
  })
  @IsNotEmpty()
  @IsString()
  reference: string;

  @ApiProperty({
    example: '50000',
    description: 'Expected amount in minor units (santim). 50000 santim = 500.00 ETB',
  })
  @IsNotEmpty()
  @IsString()
  @Matches(/^\d+$/, { message: 'expectedAmountSantim must be an integer string' })
  expectedAmountSantim: string;

  @ApiProperty({
    enum: PaymentPurpose,
    example: PaymentPurpose.SAVINGS,
    required: false,
  })
  @IsOptional()
  @IsEnum(PaymentPurpose)
  purpose?: PaymentPurpose;

  @ApiProperty({
    example: 'uuid-of-loan',
    required: false,
    description: 'Required if purpose is LOAN_REPAYMENT',
  })
  @IsOptional()
  @IsString()
  targetLoanId?: string;
}
