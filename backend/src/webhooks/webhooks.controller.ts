import {
  Controller,
  Post,
  Req,
  Headers,
  HttpCode,
  HttpStatus,
  BadRequestException,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { WebhooksService } from './webhooks.service';
import { v4 as uuidv4 } from 'uuid';

@ApiTags('Webhooks')
@Controller('webhooks')
export class WebhooksController {
  constructor(private readonly webhooksService: WebhooksService) {}

  @Post('verify-et')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Asynchronous webhook endpoint for verify.et event notifications' })
  @ApiResponse({ status: 200, description: 'Webhook event processed and acknowledged' })
  async handleVerifyEtWebhook(
    @Req() req: any,
    @Headers('x-verify-signature') sig1?: string,
    @Headers('x-hub-signature-256') sig2?: string,
    @Headers('x-verify-timestamp') timestamp?: string,
    @Headers('x-webhook-event-id') eventIdHeader?: string,
  ) {
    const signature = sig1 || sig2 || req.headers['x-verify-signature'] || '';
    if (!signature) {
      throw new BadRequestException('Missing webhook signature header.');
    }

    const eventId = eventIdHeader || req.headers['x-webhook-event-id'] || uuidv4();
    const rawBody = typeof req.body === 'string' ? req.body : JSON.stringify(req.body);

    return this.webhooksService.handleWebhook(
      rawBody,
      signature,
      timestamp,
      eventId,
      req.body,
    );
  }
}
