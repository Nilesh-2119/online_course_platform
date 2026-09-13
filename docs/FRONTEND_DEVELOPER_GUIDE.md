# 🎓 Course Platform - Frontend Developer Guide & Testing Walkthrough

Welcome to the Course Platform repository! The entire backend, authentication, VdoCipher DRM video streaming, Razorpay payment processing, and dynamic database synchronization are fully implemented and ready.

This guide provides everything you need to pull the backend, start both servers on your local machine, and test the complete end-to-end user journey.

---

## 🏗️ 1. Architecture Overview

* **Backend**: Spring Boot 3.4 (Java 23), Spring Security (JWT + Google OAuth2), Spring Data JPA, Flyway Migrations, VdoCipher DRM Integration, Razorpay Payments.
  * Runs on: `http://localhost:8080`
* **Frontend**: Next.js 15 (App Router, Tailwind CSS, TypeScript, Lucide Icons, VdoCipher Secure Player).
  * Runs on: `http://localhost:3000`
* **Database**: MySQL 8.0 (`course_platform_db`)

---

## 🚀 2. Local Machine Setup Guide

### Step 1: Prerequisites
Make sure your machine has:
* **Java 21 or 23** (`java -version`)
* **Node.js 18+** (`node -v`)
* **MySQL 8.0+** running locally on port 3306

### Step 2: Database Initialization
Open your MySQL terminal or MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS course_platform_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 3: Run the Spring Boot Backend
From the root directory:
```bash
# On Windows:
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# On macOS / Linux:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
* The backend will start on **`http://localhost:8080`**.
* Flyway will automatically create all tables, indexes, and initial course structure.
* On startup, it will automatically connect to VdoCipher and synchronize live video lessons.

### Step 4: Run the Next.js Frontend
In a new terminal window:
```bash
cd frontend
npm install
npm run dev
```
* The frontend will start on **`http://localhost:3000`**.

---

## 🧪 3. Complete End-to-End User Flow to Test

Please test the complete user journey in your browser by following these 6 steps:

### Step 1: User Registration & OTP Verification
1. Navigate to: [http://localhost:3000/register](http://localhost:3000/register)
2. Enter your Name, Email, and 10-digit Phone Number (`+91 9XXXXXXXXX`).
3. Click **Create Account**.
4. The OTP Verification screen will appear:
   * In local development, check your Spring Boot terminal console for the generated 6-digit Email & SMS OTPs (e.g. `[OTP DELIVERY - EMAIL] ... [Code: 123456]`).
   * Enter the codes and click **Verify & Complete Registration**.

### Step 2: Log In & Session Verification
1. Navigate to: [http://localhost:3000/login](http://localhost:3000/login)
2. Enter your registered email and password (or click **Continue with Google** for OAuth2).
3. Upon logging in, you will be securely redirected to the Student Dashboard.

### Step 3: Test Free Preview Video Lesson
1. On the Dashboard ([http://localhost:3000/dashboard](http://localhost:3000/dashboard)), look at the Course Curriculum.
2. Click on **Lesson 01 (Free Preview)**.
3. The VdoCipher secure HTML5 player will mount dynamically, fetch server-side DRM credentials (`otp` & `playbackInfo`), and start video streaming with dynamic student watermarking.

### Step 4: Course Purchase & Razorpay Payment Simulation
1. Click on **Buy Course** or visit [http://localhost:3000/buy](http://localhost:3000/buy).
2. Click **ENROLL NOW** to proceed to Checkout ([http://localhost:3000/checkout](http://localhost:3000/checkout)).
3. Verify that your logged-in profile (Name, Email, Phone) is automatically pre-filled.
4. Click **Proceed to Payment**:
   * The Razorpay modal will open in Test Mode.
   * Complete payment using standard test card/UPI details.
   * Upon successful payment verification, the backend instantly updates your account to **Paid Access** (`coursePurchased = true`).

### Step 5: Accessing All Full Paid Masterclass Lessons
1. You will be redirected back to your Dashboard.
2. Notice your user badge is now **UNLOCKED · FULL ACCESS**.
3. Click on **Lesson 02** or **Lesson 03**:
   * The padlock disappears and the lesson is fully playable!
   * Watch progress and lesson completion will save automatically.

### Step 6: Test Real-Time VdoCipher Auto-Sync
1. On the Dashboard Curriculum header, click the **`🔄 SYNC VDOCIPHER`** button.
2. The frontend calls `POST /api/v1/videos/sync` and refreshes the curriculum in real time based on active videos in VdoCipher.

---

## 📡 4. Key Backend API Endpoints Reference

| Category | Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/v1/auth/register` | Register new student | ❌ Public |
| **Auth** | `POST` | `/api/v1/auth/verify-otp` | Verify registration OTP | ❌ Public |
| **Auth** | `POST` | `/api/v1/auth/login` | Log in with password | ❌ Public |
| **Auth** | `GET` | `/api/v1/users/me` | Fetch active user profile | ✅ Bearer JWT |
| **Course** | `GET` | `/api/v1/courses` | List all published courses | ❌ Public |
| **Course** | `GET` | `/api/v1/courses/{id}` | Get course detail & sections | ❌ Public |
| **Video** | `GET` | `/api/v1/videos/{id}/playback` | Get VdoCipher OTP & playback token | ✅ Bearer (or Free Preview) |
| **Video** | `PUT` | `/api/v1/videos/{id}/progress` | Save video watch position | ✅ Bearer JWT |
| **Video** | `POST`| `/api/v1/videos/sync` | Force instant VdoCipher catalog sync | ❌ Public / Admin |
| **Payment**| `POST` | `/api/v1/payments/create-order` | Create Razorpay order | ✅ Bearer JWT |
| **Payment**| `POST` | `/api/v1/payments/verify` | Verify payment signature | ✅ Bearer JWT |

---

## 💡 Troubleshooting & Notes

* **JWT Tokens**: Stored in `localStorage` (`accessToken` and `refreshToken`). Attached automatically by `frontend/lib/api/client.ts` as `Authorization: Bearer <token>`.
* **VdoCipher DRM**: The video player expects `{ otp, playbackInfo }` from `GET /api/v1/videos/{id}/playback` and embeds:
  ```html
  <iframe src="https://player.vdocipher.com/v2/?otp=${otp}&playbackInfo=${playbackInfo}" />
  ```
* **Test Suite**: Run `.\mvnw.cmd test` to verify all 90 backend unit & integration tests.
