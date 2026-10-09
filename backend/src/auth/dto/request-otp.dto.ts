import { IsNotEmpty, IsOptional, IsString, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class RequestOtpDto {
  @ApiProperty({
    example: '+251911223344',
    description: 'Ethiopian phone number in E.164 format (+2519... or +2517...)',
  })
  @IsNotEmpty()
  @IsString()
  @Matches(/^\+251[79]\d{8}$/, {
    message: 'Phone number must be a valid Ethiopian mobile number (+2519XXXXXXXX or +2517XXXXXXXX)',
  })
  phoneNumber: string;

  @ApiProperty({ example: 'device_uuid_or_fingerprint', description: 'Unique device hardware identifier' })
  @IsNotEmpty()
  @IsString()
  deviceFingerprint: string;

  @ApiProperty({ example: 'Samsung Galaxy A14', required: false })
  @IsOptional()
  @IsString()
  deviceModel?: string;

  @ApiProperty({ example: 'Android 14', required: false })
  @IsOptional()
  @IsString()
  osVersion?: string;
}
