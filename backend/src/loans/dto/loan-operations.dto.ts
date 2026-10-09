import { IsNotEmpty, IsUUID, IsString, IsNumber, Min, Max } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class ApplyLoanDto {
  @ApiProperty({ description: 'ID of the loan product' })
  @IsUUID()
  @IsNotEmpty()
  productId: string;

  @ApiProperty({ description: 'Requested loan principal in minor units (santim)', example: '5000000' })
  @IsString()
  @IsNotEmpty()
  requestedAmountSantim: string;

  @ApiProperty({ description: 'Repayment tenure in months', example: 12 })
  @IsNumber()
  @Min(1)
  @Max(60)
  termMonths: number;

  @ApiProperty({ description: 'Stated loan purpose', example: 'Expansion of retail shop inventory' })
  @IsString()
  @IsNotEmpty()
  purpose: string;
}

export class CalculateLoanDto {
  @ApiProperty({ description: 'ID of the loan product' })
  @IsUUID()
  @IsNotEmpty()
  productId: string;

  @ApiProperty({ description: 'Principal in santim', example: '5000000' })
  @IsString()
  @IsNotEmpty()
  amountSantim: string;

  @ApiProperty({ description: 'Tenure in months', example: 12 })
  @IsNumber()
  @Min(1)
  @Max(60)
  termMonths: number;
}
