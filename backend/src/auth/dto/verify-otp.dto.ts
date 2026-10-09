import { IsNotEmpty, IsString, Length, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class VerifyOtpDto {
  @ApiProperty({ example: '+251911223344' })
  @IsNotEmpty()
  @IsString()
  @Matches(/^\+251[79]\d{8}$/, {
    message: 'Phone number must be a valid Ethiopian mobile number',
  })
  phoneNumber: string;

  @ApiProperty({ example: '123456', description: '6-digit OTP code' })
  @IsNotEmpty()
  @IsString()
  @Length(6, 6)
  @Matches(/^\d{6}$/, { message: 'OTP must be exactly 6 digits' })
  otp: string;

  @ApiProperty({ example: 'device_uuid_or_fingerprint' })
  @IsNotEmpty()
  @IsString()
  deviceFingerprint: string;
}
