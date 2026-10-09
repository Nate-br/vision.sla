import { IsNotEmpty, IsString, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class SetupPinDto {
  @ApiProperty({ example: '1234', description: '4 to 6 numeric digit PIN' })
  @IsNotEmpty()
  @IsString()
  @Matches(/^\d{4,6}$/, { message: 'PIN must be between 4 and 6 digits' })
  pin: string;
}

export class VerifyPinDto {
  @ApiProperty({ example: '+251911223344' })
  @IsNotEmpty()
  @IsString()
  @Matches(/^\+251[79]\d{8}$/, {
    message: 'Phone number must be a valid Ethiopian mobile number',
  })
  phoneNumber: string;

  @ApiProperty({ example: '1234' })
  @IsNotEmpty()
  @IsString()
  @Matches(/^\d{4,6}$/, { message: 'PIN must be between 4 and 6 digits' })
  pin: string;

  @ApiProperty({ example: 'device_uuid_or_fingerprint' })
  @IsNotEmpty()
  @IsString()
  deviceFingerprint: string;
}
