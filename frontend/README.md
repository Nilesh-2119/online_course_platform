# AdFix Studio

## Overview

AdFix Studio is a modern course and free-resource platform for creating better-performing advertising creative, hooks, scripts, and campaign strategies.

## Architecture

This frontend is architected for seamless production backend integration:
- **Framework:** Next.js 16 (App Router), React 19, TypeScript, TailwindCSS v4.
- **Service Boundary:** All data fetching passes through typed service modules in `lib/api/*`.
- **Domain Models:** Strict numeric types defined in `lib/types/*`. UI formatting is isolated in `lib/utils.ts`.
- **Authentication:** React `AuthProvider` with `useAuth()` hook in `lib/auth/*` ready for auth provider integration.
- **Admin Routing:** Standard Next.js nested App Router pages under `app/admin/`.
- **Mock Layer:** Centralized in `mocks/` for local prototype demonstration without modifying components.

## Getting Started

```bash
# Install dependencies
pnpm install

# Run development server
pnpm dev

# Type check
pnpm tsc --noEmit

# Lint
pnpm lint

# Production build
pnpm build
```

Open [http://localhost:3000](http://localhost:3000) to view the application.

## Documentation

* [Frontend Handoff Guide](docs/FRONTEND_HANDOFF.md)
* [Backend Integration Contracts](docs/BACKEND-INTEGRATION.md)
* [Architecture Documentation](docs/ARCHITECTURE.md)

## License

Private project.
