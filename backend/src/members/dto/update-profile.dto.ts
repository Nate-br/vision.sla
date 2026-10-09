import { IsOptional, IsString, IsEnum, Matches } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { Gender } from '../entities/customer-profile.entity';

export class UpdateProfileDto {
  @ApiProperty({ example: 'Abebe Kebede', required: false })
  @IsOptional()
  @IsString()
  fullName?: string;

  @ApiProperty({ enum: Gender, required: false })
  @IsOptional()
  @IsEnum(Gender)
  gender?: Gender;

  @ApiProperty({ example: '1990-05-14', required: false })
  @IsOptional()
  @IsString()
  dateOfBirth?: string;

  @ApiProperty({ example: 'ET-12345678', required: false })
  @IsOptional()
  @IsString()
  nationalId?: string;

  @ApiProperty({ example: 'Addis Ababa, Bole Sub-city, Woreda 03', required: false })
  @IsOptional()
  @IsString()
  address?: string;

  @ApiProperty({ example: 'Merchant', required: false })
  @IsOptional()
  @IsString()
  occupation?: string;

  @ApiProperty({ example: 'Self-employed', required: false })
  @IsOptional()
  @IsString()
  employer?: string;

  @ApiProperty({ example: '1500000', description: 'Monthly income in santim', required: false })
  @IsOptional()
  @IsString()
  monthlyIncomeSantim?: string;

  @ApiProperty({ example: 'Almaz Tadesse', required: false })
  @IsOptional()
  @IsString()
  emergencyContactName?: string;

  @ApiProperty({ example: '+251911998877', required: false })
  @IsOptional()
  @IsString()
  @Matches(/^\+251[79]\d{8}$/, { message: 'Emergency contact must be a valid Ethiopian mobile number' })
  emergencyContactPhone?: string;
}
