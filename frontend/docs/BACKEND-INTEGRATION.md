# Backend Integration Contract Specification

This document details the expected input, output, authorization requirements, and error behaviors for all backend services.

---

## 1. Authentication & Users (`lib/api/auth-service.ts`)

### `login`
* **Endpoint:** `POST /api/auth/login`
* **Input:** `{ email: string, password: string }`
* **Response (Success 200):** `{ data: UserProfile, success: true }`
* **Response (Error 401):** `{ success: false, error: "Invalid credentials" }`
* **Auth Requirement:** Public

### `createAccount`
* **Endpoint:** `POST /api/auth/register`
* **Input:** `{ email: string, name: string, password: string, phone?: string }`
* **Response (Success 201):** `{ data: UserProfile, success: true }`
* **Auth Requirement:** Public

### `completeOnboarding`
* **Endpoint:** `PUT /api/user/onboarding`
* **Input:** `{ role: string, goals: string[], experience: string }`
* **Response (Success 200):** `{ data: UserProfile, success: true }`
* **Auth Requirement:** Authenticated User

---

## 2. Courses & Access Entitlement (`lib/api/course-service.ts`)

### `getCourses`
* **Endpoint:** `GET /api/courses`
* **Response (Success 200):** `{ data: Course[], success: true }`
* **Auth Requirement:** Public

### `getCourseAccess`
* **Endpoint:** `GET /api/courses/:courseId/access`
* **Response (Success 200):** `{ data: CourseAccess, success: true }`
* **Response (Not Found 404):** `{ data: null, success: true }`
* **Auth Requirement:** Authenticated User

### `getCourseProgress`
* **Endpoint:** `GET /api/courses/:courseId/progress`
* **Response (Success 200):** `{ data: CourseProgress, success: true }`
* **Auth Requirement:** Authenticated User

---

## 3. Payments & Checkout (`lib/api/payment-service.ts`)

### `createCheckout`
* **Endpoint:** `POST /api/checkout/create-session`
* **Input:** `{ courseId: string, email: string, name: string, phone?: string, couponCode?: string }`
* **Response (Success 200):** `{ data: { orderId: string, checkoutUrl?: string, clientSecret?: string }, success: true }`
* **Auth Requirement:** Public / Authenticated
* **Important:** Course access must only be granted when the payment gateway webhook (e.g. `order.paid` event) is verified server-side.

---

## 4. Free Resources & File Downloads (`lib/api/resource-service.ts`)

### `getResources`
* **Endpoint:** `GET /api/resources`
* **Response (Success 200):** `{ data: FreeResource[], success: true }`
* **Auth Requirement:** Public

### `getResourceDownloadUrl`
* **Endpoint:** `POST /api/resources/:resourceId/download-url`
* **Response (Success 200):** `{ data: string, success: true }` (Signed S3/GCS download URL expiring in 15 minutes)
* **Auth Requirement:** Authenticated User

---

## 5. Admin Operations (`lib/api/admin-service.ts` & `lib/api/order-service.ts`)

* **Authorization Requirement for all `/api/admin/*` routes:** Authenticated Admin session with role `admin` or `superadmin`.

### `getAdminUsers`
* **Endpoint:** `GET /api/admin/users?query=...&type=...&status=...`
* **Response (Success 200):** `{ data: User[], success: true }`

### `getOrders`
* **Endpoint:** `GET /api/admin/orders`
* **Response (Success 200):** `{ data: Order[], success: true }`

### `getCoupons`
* **Endpoint:** `GET /api/admin/coupons`
* **Response (Success 200):** `{ data: Coupon[], success: true }`

### `getAnalyticsSummary`
* **Endpoint:** `GET /api/admin/analytics/summary`
* **Response (Success 200):** `{ data: AnalyticsSummary, success: true }`
