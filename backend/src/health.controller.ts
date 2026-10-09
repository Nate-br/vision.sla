import { Controller, Get } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';

@ApiTags('Health')
@Controller('health')
export class HealthController {

  @Get()
  @ApiOperation({ summary: 'Health check — used by Caddy, Podman healthcheck, and uptime monitors' })
  check() {
    return { status: 'ok', timestamp: new Date().toISOString() };
  }
}
