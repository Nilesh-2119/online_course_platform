# Course Platform API Contract & Authentication Specification

## Version: 3.0 (Authentication Upgrade: Google OIDC & Two-Channel OTP)

---

## 1. Authentication Architecture Overview

The system supports two registration & sign-in pathways:
1. **Continue with Google** via Spring Security OAuth 2.0 / OpenID Connect (`oauth2Login`) using stable subject (`sub`) identity mapping in `user_auth_providers`.
2. **Manual Registration with Two-Channel Verification** (Email OTP + Phone OTP) storing secure SHA-256 hashed codes in `verification_otps` with atomic rate limiting and single-use invalidation.

---

## 2. Account Lifecycle States

```text
[ Registration ]  ──>  PENDING_VERIFICATION (Email Unverified, Phone Unverified)
                              │
                    Verify Email & Phone OTP
                              │
                              ▼
                           ACTIVE (Full authenticated access)
                              │
                    Admin Action (if violated)
                              ▼
                           DISABLED (All access blocked)
```

* **`PENDING_VERIFICATION`**: User has registered but has not verified both contact channels. Normal password login is rejected with HTTP 403 `ACCOUNT_PENDING_VERIFICATION`.
* **`ACTIVE`**: User has completed verification or authenticated via Google OIDC. Full JWT access is granted.
* **`DISABLED`**: Account is administratively suspended. All requests return HTTP 403 `FORBIDDEN`.

---

## 3. Endpoints Specification

### 3.1 Google OAuth 2.0 / OpenID Connect Flow

#### A. Initiate Google Login
* **URL**: `GET /oauth2/authorization/google`
* **Description**: Initiates Spring Security standard OAuth2 Authorization Code flow with Google OpenID Connect.
* **Scopes**: `openid`, `profile`, `email`.

#### B. Provider Callback
* **URL**: `GET /login/oauth2/code/google`
* **Description**: Handled automatically by Spring Security. Extracts the stable `sub` claim, resolves identity via `user_auth_providers`, provisions first-time users with `ROLE_USER` and `ACTIVE` status, and redirects to frontend callback with JWT credentials:
  ```text
  http://localhost:5173/oauth/callback?accessToken={JWT}&refreshToken={UUID}&tokenType=Bearer&expiresIn=900&userName={name}&userEmail={email}&userRole=USER
  ```

---

### 3.2 Manual Registration & Two-Channel OTP

#### A. Register Account
* **Endpoint**: `POST /api/v1/auth/register`
* **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phone": "+919876543210",
    "password": "StrongPassword123!",
    "niche": "Backend Engineering",
    "businessCategory": "Software"
  }
  ```
* **Response (HTTP 201 Created)**:
  ```json
  {
    "success": true,
    "message": "Account registered. Verification codes dispatched.",
    "data": {
      "userId": 42,
      "email": "jane@example.com",
      "phone": "+919876543210",
      "verificationRequired": true,
      "requiredChannels": ["EMAIL", "PHONE"]
    },
    "timestamp": "2026-08-24T10:30:00Z"
  }
  ```

#### B. Verify OTP Channel
* **Endpoint**: `POST /api/v1/auth/verification/verify`
* **Request Body**:
  ```json
  {
    "identifier": "jane@example.com",
    "channel": "EMAIL",
    "otp": "123456"
  }
  ```
* **Response (HTTP 200 OK - Partial Verification)**:
  ```json
  {
    "success": true,
    "message": "EMAIL verified successfully. Please complete remaining channel verification.",
    "data": {
      "channel": "EMAIL",
      "verified": true,
      "emailVerified": true,
      "phoneVerified": false,
      "accountActivated": false
    },
    "timestamp": "2026-08-24T10:30:15Z"
  }
  ```
* **Response (HTTP 200 OK - Full Activation)**:
  ```json
  {
    "success": true,
    "message": "Account successfully verified and activated! Welcome to Course Platform.",
    "data": {
      "channel": "PHONE",
      "verified": true,
      "emailVerified": true,
      "phoneVerified": true,
      "accountActivated": true,
      "auth": {
        "accessToken": "eyJhbGciOi...",
        "refreshToken": "48b625...",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "user": {
          "id": 42,
          "name": "Jane Doe",
          "email": "jane@example.com",
          "role": "USER",
          "status": "ACTIVE"
        }
      }
    },
    "timestamp": "2026-08-24T10:30:30Z"
  }
  ```

#### C. Resend Verification Code
* **Endpoint**: `POST /api/v1/auth/verification/resend`
* **Request Body**:
  ```json
  {
    "identifier": "jane@example.com",
    "channel": "EMAIL"
  }
  ```
* **Response (HTTP 200 OK)**:
  ```json
  {
    "success": true,
    "message": "Verification code resent successfully.",
    "timestamp": "2026-08-24T10:31:00Z"
  }
  ```
* **Cooldown Rate Limit (HTTP 429 Too Many Requests)**:
  ```json
  {
    "timestamp": "2026-08-24T10:31:10Z",
    "status": 429,
    "error": "Too Many Requests",
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Please wait 60 seconds before requesting a new code."
  }
  ```

---

### 3.3 Login & Session Management

#### A. Authenticate with Credentials
* **Endpoint**: `POST /api/v1/auth/login`
* **Request Body**:
  ```json
  {
    "email": "jane@example.com",
    "password": "StrongPassword123!"
  }
  ```
* **Response (HTTP 200 OK)**:
  ```json
  {
    "success": true,
    "message": "Login successful",
    "data": {
      "accessToken": "eyJhbGciOi...",
      "refreshToken": "48b625...",
      "tokenType": "Bearer",
      "expiresIn": 900,
      "user": {
        "id": 42,
        "name": "Jane Doe",
        "email": "jane@example.com",
        "role": "USER",
        "status": "ACTIVE"
      }
    }
  }
  ```
* **Pending Account Response (HTTP 403 Forbidden)**:
  ```json
  {
    "timestamp": "2026-08-24T10:32:00Z",
    "status": 403,
    "error": "Forbidden",
    "code": "ACCOUNT_PENDING_VERIFICATION",
    "message": "Account verification is required before login. Please complete Email and Phone OTP verification."
  }
  ```
