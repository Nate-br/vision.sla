import {
  Controller,
  Get,
  Put,
  Post,
  Delete,
  Body,
  Req,
  UseGuards,
  BadRequestException,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiConsumes } from '@nestjs/swagger';
import { MembersService } from './members.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { KycDocumentType } from './entities/kyc-document.entity';

@ApiTags('Members')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('members')
export class MembersController {
  constructor(private readonly membersService: MembersService) {}

  @Get('me')
  @ApiOperation({ summary: 'Get current member profile and KYC document state' })
  async getMyProfile(@Req() req: any) {
    return this.membersService.getProfile(req.user.id);
  }

  @Put('me/profile')
  @ApiOperation({ summary: 'Update member profile details' })
  async updateMyProfile(@Req() req: any, @Body() dto: UpdateProfileDto) {
    return this.membersService.updateProfile(req.user.id, dto);
  }

  @Post('me/kyc')
  @ApiConsumes('multipart/form-data')
  @ApiOperation({ summary: 'Upload KYC identity document (National ID / Passport / Selfie)' })
  async uploadKycDocument(@Req() req: any) {
    const isMultipart = typeof req.isMultipart === 'function' ? req.isMultipart() : false;
    if (!isMultipart) {
      throw new BadRequestException('Request must be multipart/form-data');
    }

    const data = await req.file();
    if (!data) {
      throw new BadRequestException('No file uploaded');
    }

    const rawDocumentType = (data.fields?.documentType as any)?.value || 'NATIONAL_ID_FRONT';
    if (!Object.values(KycDocumentType).includes(rawDocumentType as KycDocumentType)) {
      throw new BadRequestException(`Invalid documentType: ${rawDocumentType}`);
    }

    const buffer = await data.toBuffer();
    return this.membersService.processAndStoreKycDocument(
      req.user.id,
      rawDocumentType as KycDocumentType,
      data.filename,
      buffer,
      data.mimetype,
    );
  }

  @Delete('me')
  @ApiOperation({ summary: 'Request account deletion and personal data erasure (S-507)' })
  async deleteAccount(@Req() req: any) {
    return this.membersService.deleteAccount(req.user.id);
  }
}
