import {
  Controller,
  Post,
  Get,
  Delete,
  Body,
  Req,
  Param,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { RequestOtpDto } from './dto/request-otp.dto';
import { VerifyOtpDto } from './dto/verify-otp.dto';
import { SetupPinDto, VerifyPinDto } from './dto/pin.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { JwtAuthGuard } from './jwt-auth.guard';

@ApiTags('Authentication')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('otp/request')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Request OTP challenge for login or sign-up' })
  @ApiResponse({ status: 200, description: 'OTP challenge initiated successfully' })
  async requestOtp(@Body() dto: RequestOtpDto, @Req() req: any) {
    const ipAddress = req.ip || req.headers['x-forwarded-for'] || '127.0.0.1';
    return this.authService.requestOtp(dto, ipAddress);
  }

  @Post('otp/verify')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Verify OTP code and authenticate device' })
  @ApiResponse({ status: 200, description: 'OTP verified, session issued' })
  async verifyOtp(@Body() dto: VerifyOtpDto, @Req() req: any) {
    const ipAddress = req.ip || req.headers['x-forwarded-for'] || '127.0.0.1';
    return this.authService.verifyOtp(dto, ipAddress);
  }

  @Post('pin/setup')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Set up or change member PIN (requires authentication)' })
  @ApiResponse({ status: 200, description: 'PIN set successfully' })
  async setupPin(@Body() dto: SetupPinDto, @Req() req: any) {
    return this.authService.setupPin(req.user.id, dto);
  }

  @Post('pin/verify')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Authenticate member with phone number and PIN' })
  @ApiResponse({ status: 200, description: 'PIN verified, session issued' })
  async verifyPin(@Body() dto: VerifyPinDto, @Req() req: any) {
    const ipAddress = req.ip || req.headers['x-forwarded-for'] || '127.0.0.1';
    return this.authService.verifyPin(dto, ipAddress);
  }

  @Post('refresh')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Rotate refresh token and issue new access token' })
  @ApiResponse({ status: 200, description: 'Tokens rotated successfully' })
  async refreshTokens(@Body() dto: RefreshTokenDto) {
    return this.authService.refreshToken(dto);
  }

  @Post('logout')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Invalidate current member session' })
  @ApiResponse({ status: 200, description: 'Logged out successfully' })
  async logout(@Req() req: any) {
    const sessionId = req.user.sessionId || req.user.familyId;
    return this.authService.logout(sessionId, req.user.id);
  }

  @Get('sessions')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'List active login sessions for current member' })
  @ApiResponse({ status: 200, description: 'List of active sessions' })
  async getActiveSessions(@Req() req: any) {
    return this.authService.getActiveSessions(req.user.id);
  }

  @Delete('sessions/:sessionId')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Revoke a remote device session' })
  @ApiResponse({ status: 200, description: 'Session revoked' })
  async revokeSession(@Param('sessionId') sessionId: string, @Req() req: any) {
    return this.authService.logout(sessionId, req.user.id);
  }
}
