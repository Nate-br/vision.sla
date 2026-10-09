# Vision for a Better Life SLA — Mobile App

Ethiopian savings-and-loan association (SLA) member app.  
Android (Kotlin/Compose) + NestJS backend with verify.et payment verification.

## Repository layout

```
android/   — Kotlin/Jetpack Compose multi-module app
backend/   — NestJS (TypeScript, Fastify) API + Podman deployment
docs/      — Architecture, API spec, security checklist, UI spec
plan.md    — Master engineering plan (source of truth)
```

## Quick start

### Backend
```bash
cd backend
cp .env.example .env          # fill in real values
npm install
npm run start:dev
```

### Android
```bash
cd android
./gradlew assembleDebug
```

## Documentation
- [Engineering Plan](plan.md)
- [API Spec (OpenAPI)](docs/openapi.yaml)
- [Security Checklist](docs/SECURITY_CHECKLIST.md)
- [UI Spec](docs/ui-spec.md)

## Branching & commits
- Trunk-based development, short-lived feature branches
- [Conventional Commits](https://www.conventionalcommits.org/) required
- See [CONTRIBUTING.md](CONTRIBUTING.md)

## Licence
Private — Vision for a Better Life SLA. All rights reserved.
