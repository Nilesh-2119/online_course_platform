# 🚀 Online Course Platform - Fullstack

A modern, production-grade online course platform featuring Spring Boot (Java 23), Next.js 15, MySQL 8.0, Razorpay payment processing, and VdoCipher DRM video streaming with dynamic watermarking.

---

## 📖 Quick Links & Documentation

* **Frontend Developer & Testing Guide**: [`docs/FRONTEND_DEVELOPER_GUIDE.md`](./docs/FRONTEND_DEVELOPER_GUIDE.md)
* **Backend Architecture & Security Implementation**: [`src/main/resources/application.yml`](./src/main/resources/application.yml)

---

## ⚡ Quick Start

### 1. Start Backend (Port 8080)
```bash
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Start Frontend (Port 3000)
```bash
cd frontend
npm install
npm run dev
```

---

## 🧪 Testing the Complete Flow
Refer to [`docs/FRONTEND_DEVELOPER_GUIDE.md`](./docs/FRONTEND_DEVELOPER_GUIDE.md) for the 6-step testing walkthrough:
1. Registration & OTP Verification
2. Login & Session Setup
3. Free Video Preview Playback
4. Razorpay Course Purchase Simulation
5. Full Masterclass Access & DRM Video Streaming
6. Dynamic VdoCipher Video Syncing
