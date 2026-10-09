import { NestFactory } from '@nestjs/core';
import {
  FastifyAdapter,
  NestFastifyApplication,
} from '@nestjs/platform-fastify';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { ValidationPipe, VersioningType } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import helmet from 'helmet';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create<NestFastifyApplication>(
    AppModule,
    new FastifyAdapter({ logger: false }), // logging handled by nestjs-pino
  );

  const config = app.get(ConfigService);
  const port = config.get<number>('PORT', 3000);
  const isProd = config.get('NODE_ENV') === 'production';

  // ─── Security headers ────────────────────────────────────────────────────
  await app.register(helmet as any, {
    contentSecurityPolicy: isProd,
  });

  // ─── CORS ────────────────────────────────────────────────────────────────
  // API is called only by our Android app and admin web — strict origin list
  app.enableCors({
    origin: isProd
      ? [`https://${config.get('PUBLIC_DOMAIN')}`]
      : ['http://localhost:3000', 'http://localhost:5173'],
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
    credentials: true,
  });

  // ─── Global validation ───────────────────────────────────────────────────
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,       // strip unknown properties
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: { enableImplicitConversion: true },
    }),
  );

  // ─── API versioning ──────────────────────────────────────────────────────
  app.enableVersioning({ type: VersioningType.URI, defaultVersion: '1' });

  // ─── OpenAPI (dev/staging only) ──────────────────────────────────────────
  if (!isProd) {
    const swaggerConfig = new DocumentBuilder()
      .setTitle('VBL API')
      .setDescription('Vision for a Better Life SLA — Member API')
      .setVersion('1')
      .addBearerAuth()
      .addApiKey({ type: 'apiKey', in: 'header', name: 'Idempotency-Key' }, 'idempotency-key')
      .build();
    const document = SwaggerModule.createDocument(app, swaggerConfig);
    SwaggerModule.setup('api/docs', app, document);
  }

  // ─── Webhook endpoint: raw body needed for HMAC verification ─────────────
  // Handled in WebhooksModule via fastify rawBody option

  await app.listen(port, '0.0.0.0');
}

bootstrap();
