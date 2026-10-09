# Multi-stage Dockerfile for VBL NestJS backend
# Target 'production' is used in compose.yaml
# RAM budget: 400MB container limit, --max-old-space-size=300 (plan Section 3.1)

# ─── Stage 1: dependencies ───────────────────────────────────────────────────
FROM node:22-alpine AS deps
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --only=production && npm cache clean --force

# ─── Stage 2: builder ────────────────────────────────────────────────────────
FROM node:22-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json tsconfig.json nest-cli.json ./
RUN npm ci

COPY src ./src
RUN npm run build

# ─── Stage 3: production image ───────────────────────────────────────────────
FROM node:22-alpine AS production
LABEL org.opencontainers.image.title="VBL API"
LABEL org.opencontainers.image.description="Vision for a Better Life SLA backend"

# Run as non-root user (rootless Podman maps this to a sub-UID)
RUN addgroup -S vbl && adduser -S vbl -G vbl

WORKDIR /app

# Only production deps
COPY --from=deps /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist

# Uploads directory — mounted as a volume in compose.yaml
RUN mkdir -p /app/uploads && chown vbl:vbl /app/uploads

USER vbl

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --spider -q http://localhost:3000/v1/health || exit 1

CMD ["node", "--max-old-space-size=300", "dist/main"]
