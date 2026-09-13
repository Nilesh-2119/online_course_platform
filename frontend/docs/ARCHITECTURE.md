# Architecture

## Application Structure

AdFix Studio uses the Next.js App Router:
* `app/`: Next.js App Router route segments.
* `components/`: Modular feature components (`admin/`, `auth/`, `dashboard/`, `purchase/`, `marketing/`, `shared/`, `ui/`).
* `lib/`: Domain types (`lib/types/`), service layer (`lib/api/`), auth context (`lib/auth/`), and utility formatters (`lib/utils.ts`).
* `mocks/`: Isolated mock seed data and async response adapter.
* `docs/`: Comprehensive integration and handoff specifications.

## Public Website

The homepage composes marketing sections from `components/adfix-sections.tsx` with `Navigation` and `Footer` components. Public purchase and authentication screens use modular views under `components/purchase/` and `components/auth/`.

## User Dashboard

`/dashboard` renders user courses, free resources, and account management views via `components/dashboard/dashboard-view.tsx`. It consumes the `useAuth()` hook and course/resource services.

## Admin Dashboard

`/admin` and its nested routes (`/admin/users`, `/admin/courses`, `/admin/resources`, `/admin/orders`, `/admin/coupons`, `/admin/analytics`, `/admin/settings`) render the admin panel using modular components under `components/admin/`.

## Service & Data Layer

UI components never import mock data directly. All data access is routed through `lib/api/*` service functions (`courseService`, `resourceService`, `orderService`, etc.) returning typed promises.

## Backend Integration Boundaries

See `docs/BACKEND-INTEGRATION.md` and `docs/FRONTEND_HANDOFF.md` for full integration contracts.
