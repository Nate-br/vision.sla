import { IsNotEmpty, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class RefreshTokenDto {
  @ApiProperty({ description: 'Refresh token issued during login' })
  @IsNotEmpty()
  @IsString()
  refreshToken: string;

  @ApiProperty({ description: 'Device fingerprint' })
  @IsNotEmpty()
  @IsString()
  deviceFingerprint: string;
}
