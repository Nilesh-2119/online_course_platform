# AdFix Studio — Frontend Developer Handoff Document

This document provides a comprehensive technical guide for backend engineers integrating authentication, databases, payment gateways, video infrastructure, file storage, and APIs into the AdFix Studio frontend.

---

## 1. Architecture Overview

```
Frontend Components (UI)
        ↓
Feature Services (`lib/api/*`)
        ↓
Domain Models & Types (`lib/types/*`)
        ↓
Backend API / Provider Gateways (Future)
```

The frontend uses **Next.js 16 (App Router)**, **React 19**, and **TailwindCSS v4**. All components consume services from `lib/api/*` rather than reading raw data directly, ensuring seamless backend integration without altering UI presentation.

---

## 2. Directory Structure

* **`app/`**: Next.js App Router route segments.
  * `(marketing)/`: Landing page (`/`), `/free-resources`, legal pages (`/terms`, `/privacy`, `/refund-policy`, `/cookie-policy`, `/course-usage`, `/disclaimer`, `/contact`).
  * `(auth & purchase)`: `/buy`, `/checkout`, `/account-setup`, `/create-account`, `/login`, `/onboarding`.
  * `dashboard/`: Authenticated user dashboard (`/dashboard`).
  * `admin/`: Modular admin portal routes (`/admin`, `/admin/users`, `/admin/courses`, `/admin/resources`, `/admin/orders`, `/admin/coupons`, `/admin/analytics`, `/admin/settings`).
* **`components/`**: Modularized presentation components.
  * `admin/`: Admin shell, overview, users, courses, resources, orders, coupons, analytics, and settings views.
  * `auth/`: Login, account creation, account setup, and onboarding views.
  * `dashboard/`: Courses, free resources, and account tabs.
  * `purchase/`: Buy and checkout views.
  * `marketing/`: Homepage sections (`adfix-sections.tsx`), header (`navigation.tsx`), and footer (`footer.tsx`).
  * `shared/`: Shared flow shell (`flow-shell.tsx`) and legal page template (`legal-page.tsx`).
  * `ui/`: Radix-based design system primitives.
* **`lib/`**:
  * `types/`: Strict TypeScript domain models (`user.ts`, `course.ts`, `resource.ts`, `order.ts`, `coupon.ts`, `analytics.ts`, `api.ts`).
  * `api/`: Service boundary modules (`auth-service.ts`, `course-service.ts`, `resource-service.ts`, `payment-service.ts`, `order-service.ts`, `coupon-service.ts`, `admin-service.ts`, `analytics-service.ts`).
  * `auth/`: React `AuthProvider` and `useAuth()` hook for user state management.
  * `utils.ts`: Class merging (`cn`), currency formatting (`formatCurrency`), date formatting (`formatDate`, `formatTime`), and number formatting (`formatNumber`).
* **`mocks/`**:
  * `seed-data.ts`: Central seed data source for prototype demonstration.
  * `mock-adapter.ts`: Async delay and response simulator.
* **`docs/`**: Technical documentation.

---

## 3. Data Models (`lib/types/*`)

All domain models use **raw numeric values** for prices, percentages, and metrics. UI formatting is applied strictly at the presentation layer using `lib/utils.ts`.

| Model | File | Key Fields | Notes |
| :--- | :--- | :--- | :--- |
| `User` | `lib/types/user.ts` | `id`, `name`, `email`, `type`, `role`, `status`, `joinedAt` | Status: `active` \| `suspended` \| `deleted` |
| `UserProfile` | `lib/types/user.ts` | Extends `User`, `onboardingComplete`, `onboardingRole`, `onboardingGoals` | Captures onboarding survey |
| `Course` | `lib/types/course.ts` | `id`, `title`, `price` (number), `currency`, `lessonCount`, `studentCount` | Currency defaults to `"INR"` |
| `CourseLesson` | `lib/types/course.ts` | `id`, `courseId`, `title`, `order`, `status`, `hasPreview`, `videoId` | Compatible with external video host |
| `FreeResource` | `lib/types/resource.ts` | `id`, `title`, `type`, `fileName`, `fileSize`, `downloadUrl` | Download URL generated on-demand |
| `Order` | `lib/types/order.ts` | `id`, `userId`, `amount`, `discountAmount`, `finalAmount`, `status` | Status: `paid` \| `pending` \| `failed` |
| `Coupon` | `lib/types/coupon.ts` | `code`, `discountType`, `discountValue`, `usageCount`, `usageLimit` | `percentage` or `fixed` |
| `AnalyticsSummary` | `lib/types/analytics.ts` | `totalUsers`, `totalRevenue`, `conversionRate`, `completionRate` | Numeric values for dashboards |

---

## 4. API & Service Integration Points (`lib/api/*`)

Every service module contains explicit `BACKEND_INTEGRATION_POINT` annotations. Replace the mock implementation in each file with real API calls (e.g. `fetch("/api/...")` or Server Actions):

### `auth-service.ts`
* `login({ email, password })` → POST `/api/auth/login`
* `createAccount({ email, name, password })` → POST `/api/auth/register`
* `completeOnboarding({ role, goals, experience })` → PUT `/api/user/onboarding`
* `getCurrentUser()` → GET `/api/user/me`
* `logout()` → POST `/api/auth/logout`

### `course-service.ts`
* `getCourses()` → GET `/api/courses`
* `getCourse(id)` → GET `/api/courses/:id`
* `getCourseLessons(courseId)` → GET `/api/courses/:id/lessons`
* `getCourseAccess(courseId)` → GET `/api/courses/:id/access` (Server-verified entitlement)
* `getCourseProgress(courseId)` → GET `/api/courses/:id/progress`

### `resource-service.ts`
* `getResources()` → GET `/api/resources`
* `getResourceDownloadUrl(resourceId)` → POST `/api/resources/:id/download-url` (Returns signed S3/GCS download URL)

### `payment-service.ts`
* `createCheckout(request)` → POST `/api/checkout/create-session` (Returns Razorpay order ID or Stripe session URL)
* `verifyPayment(orderId)` → POST `/api/checkout/verify` (Must be verified via webhook on backend)

### `order-service.ts`
* `getOrders()` → GET `/api/admin/orders`
* `getOrder(id)` → GET `/api/admin/orders/:id`
* `getUserOrders()` → GET `/api/user/orders`

### `coupon-service.ts`
* `getCoupons()` → GET `/api/admin/coupons`
* `validateCoupon(code)` → POST `/api/coupons/validate` (Server-side validation)

### `admin-service.ts`
* `getAdminUsers(filters)` → GET `/api/admin/users`
* `getAdminUser(id)` → GET `/api/admin/users/:id`
* `getAdminProfile()` → GET `/api/admin/profile`
* `getAdminSessions()` → GET `/api/admin/sessions`
* `getAuditLog()` → GET `/api/admin/audit-log`

### `analytics-service.ts`
* `getAnalyticsSummary()` → GET `/api/admin/analytics/summary`
* `getAnalyticsCharts()` → GET `/api/admin/analytics/charts`
* `getKPIs()` → GET `/api/admin/analytics/kpis`
* `getRecentActivity()` → GET `/api/admin/analytics/activity`

---

## 5. Authentication & Security Architecture

1. **Client-Side State:** `AuthProvider` (`lib/auth/auth-context.tsx`) provides `user`, `isAuthenticated`, `isLoading`, and `role` to UI components.
2. **No Password Storage:** Passwords are never stored in browser storage (`localStorage` or `sessionStorage`).
3. **Route Protection (Middleware):** Implement a standard Next.js `middleware.ts` to enforce server-side authentication on `/admin/*` and `/dashboard`.

---

## 6. How to Replace Mock Services with Real Backend APIs

1. Open the target service file in `lib/api/` (e.g. `course-service.ts`).
2. Replace `mockSuccess(...)` calls with `fetch` requests or Server Actions calling your backend API.
3. Remove the import of `mock-adapter.ts` and `seed-data.ts`.
4. When all services are connected, the `mocks/` directory can be safely deleted.

---

## 7. Build & Validation Commands

```bash
# Install dependencies
pnpm install

# Run TypeScript type check
pnpm tsc --noEmit

# Run ESLint check
pnpm lint

# Build for production
pnpm build

# Start production server
pnpm start
```
