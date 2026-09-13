# Enterprise Application Security Audit & Hardening Specification

**Target Platform:** Online Course & Video Streaming Platform  
**Runtime:** Java 21 LTS | Spring Boot 3.4.2 | Spring Security 6  
**Architecture:** Modular Monolith with Stateless JWT Authentication & Relational Ledger  
**Date:** August 2026

---

## 1. Executive Summary

This document details the security architecture, threat model mitigations, cryptographic verification mechanics, audit results, and production deployment requirements for the backend platform.

The system is designed with a **Zero-Trust Client Model**:
- The backend never trusts client assertions regarding identity, pricing, payment success, or video/course completion.
- Sensitive credentials (VdoCipher API secrets, Razorpay API secrets, JWT signing keys) remain strictly on the backend server.
- All monetary operations and content entitlements are validated authoritatively against relational database ledgers.

---

## 2. Authentication & Session Security Audit

### 2.1 Password Hashing & Storage
- **Algorithm:** Adaptive `BCryptPasswordEncoder` (Spring Security recommended).
- **Guarantees:** Salted per-user, computationally intensive against brute-force and dictionary attacks. Plaintext passwords are never stored in the database or returned in API responses.

### 2.2 JWT Access Token Management
- **Signature Algorithm:** HMAC-SHA256 (`HmacSHA256`) with a cryptographically secure 256-bit+ secret.
- **Payload Sanitization:** Contains only immutable subject claims (`sub` = email, `userId`, `role`, `iss`, `iat`, `exp`). Sensitive attributes like password hashes, phone numbers, or internal keys are never embedded.
- **Short-Lived Expiration:** Default TTL of 15 minutes (`900s`), minimizing window of exposure if a client token is intercepted.

### 2.3 Refresh Token Rotation & Replay Attack Defense
- **Hashed Storage:** Refresh tokens are never stored in plaintext. They are stored exclusively as one-way SHA-256 hashes (`TokenHasher.hash(rawToken)`).
- **Token Rotation:** Every invocation of `/api/v1/auth/refresh` revokes the old refresh token and issues a new pair.
- **Reuse Detection & Session Invalidation:** If an already-rotated or revoked refresh token is presented (indicating a potential token theft or replay attack), the system immediately revokes **all** active refresh tokens for that user ID.
- **Logout:** Explicitly revokes active refresh tokens immediately on the server.

---

## 3. Centralized Authorization & Entitlement Engine (`CourseAccessService`)

### 3.1 Multi-Course Isolation (No `users.is_paid` global flag)
- Course access is evaluated on a strict per-course basis against the `course_purchases` ledger where `status = 'SUCCESS'`.
- Purchasing **Course A** grants access only to Course A. Attempts to stream paid lessons in Course B or Course C return `403 Forbidden` (`COURSE_ACCESS_DENIED`).

### 3.2 Gatekeeping Sequence for Video Playback
Every video stream request executes the following authoritative verification chain:
1. **Entity Resolution:** Single-roundtrip JOIN FETCH of Video $\to$ Section $\to$ Course to eliminate N+1 latency.
2. **Publishing Status Check:** Rejects `DRAFT` or `ARCHIVED` videos/courses for regular users (404/403).
3. **User Account Health:** Blocked/disabled accounts (`status == DISABLED`) are rejected immediately.
4. **Free Content Check:** Videos marked `is_free = true` are allowed for preview without requiring purchase.
5. **Purchase Verification:** Videos marked `is_free = false` require verified authentication and active purchase records.

---

## 4. Payment Security & Cryptographic Verification

### 4.1 Price Tampering Immunity
- The client checkout endpoint (`POST /api/v1/payments/orders`) accepts **only** `courseId`.
- Any amount or currency sent in client payloads is ignored.
- The authoritative price is queried directly from `courses.price` in the database and dynamically converted to subunits (paise = $\text{price} \times 100$) before invoking Razorpay.

### 4.2 Constant-Time HMAC-SHA256 Signature Verification
- Frontend payment verification (`POST /api/v1/payments/verify`) validates:
  $$\text{HMAC-SHA256}(\text{razorpayOrderId} \parallel \text{"|"} \parallel \text{razorpayPaymentId}, \text{keySecret})$$
- Uses `MessageDigest.isEqual` to enforce constant-time byte comparison, eliminating side-channel timing attack vectors.

### 4.3 Raw Webhook Body Verification & Idempotency
- Webhooks (`POST /api/v1/payments/webhook`) validate `X-Razorpay-Signature` against the exact, unparsed request payload string (`@RequestBody String rawPayload`).
- Out-of-order deliveries and repeated webhook retries are handled idempotently; orders already marked `SUCCESS` retain their original timestamps and do not create duplicate entitlements.

---

## 5. VdoCipher Video DRM & Playback Authorization

### 5.1 Zero Secret Exposure
- `VDOCIPHER_API_SECRET` is never stored in database tables, never sent to frontend players, and never logged in error/trace statements.
- The frontend player interacts solely with short-lived, backend-generated `otp` and `playbackInfo` tokens.

### 5.2 Dynamic Playback Policy
- **Configurable TTL:** Enforces short-lived OTP expiry (default 300s) to prevent playback token harvesting.
- **Domain Whitelisting:** Optional URL whitelist parameter restricting iframe player playback to approved frontend domains.
- **Fail-Safe Gateways:** In the event of upstream VdoCipher outages or API rejections, the backend fails closed (returns HTTP 500 `VIDEO_PLAYBACK_ERROR`) without leaking provider API secrets or granting unverified access.

---

## 6. HTTP Security Headers & Network Protections

Configured in [SecurityConfig.java](file:///c:/Users/NILESH%20KALE/Documents/CP/src/main/java/com/courseplatform/config/SecurityConfig.java):
1. **HTTP Strict Transport Security (HSTS):** `max-age=31536000; includeSubDomains` enforcing HTTPS connections.
2. **Clickjacking Protection:** `X-Frame-Options: DENY` preventing unauthorized iframe embedding of backend endpoints.
3. **MIME-Type Sniffing Protection:** `X-Content-Type-Options: nosniff`.
4. **CORS Hardening:** Configurable allowed origins (`app.cors.allowed-origins`), credentials support, and restricted HTTP verbs.

---

## 7. Error Handling & Information Leakage Prevention

Configured in [GlobalExceptionHandler.java](file:///c:/Users/NILESH%20KALE/Documents/CP/src/main/java/com/courseplatform/common/GlobalExceptionHandler.java):
- Unhandled exceptions return generic, safe JSON error objects (`ErrorCode.INTERNAL_SERVER_ERROR`).
- Stack traces, SQL exception messages, internal server paths, and table names are stripped from all API responses.
- Type mismatches and malformed JSON payloads return structured `400 Bad Request` responses.

---

## 8. Residual Risks & Production Hardening Checklist

Before deploying to production environments:

| Area | Requirement | Status / Action |
| :--- | :--- | :--- |
| **TLS/HTTPS** | Terminate TLS with valid certificates on Reverse Proxy / Load Balancer. | Mandatory in Production |
| **Secrets Management** | Inject `JWT_SECRET`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, and `VDOCIPHER_API_SECRET` via cloud secret managers (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets). | Mandatory in Production |
| **Database Credentials** | Rotate production MySQL passwords and restrict database network access to private VPC subnets. | Mandatory in Production |
| **WAF & Rate Limiting** | Deploy Cloudflare or AWS WAF in front of auth endpoints (`/api/v1/auth/**`) for distributed DDoS and IP-based rate limiting. | Recommended |
| **Audit Logging** | Forward Spring Boot JSON log outputs to centralized SIEM/CloudWatch for anomaly monitoring. | Recommended |
