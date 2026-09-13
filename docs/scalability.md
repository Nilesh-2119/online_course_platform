# Production Scalability & High-Concurrency Architecture

**Target Deployment:** Multi-Instance Clustered Backend  
**Runtime:** Java 21 LTS | Spring Boot 3.4.2 | MySQL 8+  
**Edge & Third-Party Integration:** Cloudflare / AWS ALB + VdoCipher CDN + Razorpay Gateway  
**Date:** August 2026

---

## 1. Target Infrastructure Topology

The platform is designed to scale horizontally across multiple stateless Spring Boot application nodes behind an HTTP load balancer without requiring sticky sessions or node affinity.

```
                         [ Internet Clients (Web / Mobile) ]
                                         │
                                         ▼
                         [ Cloudflare / AWS ALB (TLS + WAF) ]
                                         │
                 ┌───────────────────────┼───────────────────────┐
                 │ Round-Robin           │ Round-Robin           │ Round-Robin
                 ▼                       ▼                       ▼
      [ Spring Boot Node 1 ]  [ Spring Boot Node 2 ]  [ Spring Boot Node 3 ]
                 │                       │                       │
                 └───────────────────────┼───────────────────────┘
                                         ▼
                             [ Managed MySQL Cluster ]
                             (Primary Read-Write + Replicas)
                                         │
                     ┌───────────────────┴───────────────────┐
                     ▼                                       ▼
        [ VdoCipher API / CDN ]                     [ Razorpay Gateway ]
    (Video DRM & Global Streaming)            (Order & Payment Settlement)
```

---

## 2. Statelessness Audit & Multi-Instance Compatibility

| Component | State Management Pattern | Multi-Instance Behavior |
| :--- | :--- | :--- |
| **User Authentication** | Stateless JWT (`HmacSHA256`) | Any Spring Boot node can authenticate and authorize incoming requests without inter-node session replication. |
| **Refresh Tokens** | Database-Backed Relational Ledger (`refresh_tokens`) | Hashed tokens (`SHA-256`) stored in MySQL allow any instance to validate, rotate, or revoke sessions. |
| **Local Memory** | Zero in-memory business state | No in-memory HTTP sessions (`SessionCreationPolicy.STATELESS`); state is externalized to MySQL. |
| **Local Filesystem** | Zero local disk dependencies | The application does not store user uploads or video chunks on local disk. |
| **Instance Affinity** | Not required | Load balancer can distribute requests via standard round-robin or least-connections. |

---

## 3. Database Performance & Query Optimization

### 3.1 Relational Index Strategy
The database schema utilizes composite indexes tailored to high-throughput access patterns:
* **Entitlement Verification:** `idx_purchases_user_course_status (user_id, course_id, status)` enables $O(1)$ index-seek lookups for `hasUserPurchasedCourse`.
* **Curriculum Rendering:** `idx_course_sections_course (course_id, display_order)` and `idx_videos_section (section_id, display_order)` ensure sequential index scans.
* **Progress Tracking:** Composite unique index `uk_video_progress_user_video (user_id, video_id)` prevents duplicate records and optimizes single-row upserts.

### 3.2 Eliminating N+1 Query Hazards
* `CourseSectionRepository.findSectionsWithVideosByCourseId` uses `LEFT JOIN FETCH s.videos v` to retrieve the entire course section and lesson hierarchy in a **single roundtrip** to the database, eliminating the classic $1 + N$ query problem during catalog browsing.

### 3.3 Transaction Boundary Control
* Queries on catalog browsing, free resource listing, and progress retrieval are annotated with `@Transactional(readOnly = true)` to avoid dirty-checking overhead in Hibernate and utilize MySQL read transactions efficiently.
* Read-write transactions (`createOrder`, `verifyPayment`, `updateVideoProgress`) are short and strictly bounded.

---

## 4. High-Traffic Endpoints & Write-Load Strategy

### 4.1 Video Progress Tracking (`PUT /api/v1/videos/{id}/progress`)
* **Problem:** If a video player sends progress updates every second, $10,000$ concurrent viewers would generate $10,000$ writes/second to MySQL.
* **Solution (Throttled Heartbeats):**
  1. Frontend player buffers playback position and transmits progress updates periodically (**every 10–15 seconds**) and upon pause/exit events.
  2. Backend performs an efficient single-row update on `video_progress` matching `(user_id, video_id)`.
  3. No distributed Redis buffer is needed at initial scale; indexed relational updates comfortably handle thousands of active concurrent viewers with periodic heartbeats.

### 4.2 Decoupled Video Streaming (VdoCipher DRM)
* **Problem:** Streaming multi-gigabyte video files directly through Spring Boot saturates server bandwidth, thread pools, and CPU.
* **Solution:**
  * Spring Boot **never proxies or streams video bytes**.
  * The backend's sole responsibility is authorization and issuing short-lived `otp` and `playbackInfo` tokens ($\approx 200\text{ bytes}$).
  * High-bandwidth video playback, adaptive bitrate chunk delivery, and global CDN caching are offloaded entirely to VdoCipher's infrastructure.

### 4.3 Payment Verification & Webhook Concurrency
* **Problem:** Frontend verification (`POST /verify`) and gateway webhooks (`POST /webhook`) may arrive simultaneously for the same order.
* **Solution:**
  * Both handlers verify state idempotently against the database: if `purchase.getStatus() == SUCCESS`, the second request is recognized as a completed duplicate and returns immediately without duplicate processing or lock contention.

---

## 5. Caching Roadmap

As user traffic expands, the following read-heavy, low-frequency mutation endpoints can adopt a **Cache-Aside** strategy:

| Data Entity | Volatility | Recommended Cache Strategy | Cache Key / TTL |
| :--- | :--- | :--- | :--- |
| **Published Course Catalog** | Very Low (Changes on admin edits) | In-Memory (Caffeine) or Redis | `courses::published` (TTL: 1 hour, evicted on course update) |
| **Course Section Hierarchy** | Very Low | In-Memory (Caffeine) or Redis | `course::sections::{courseId}` (TTL: 1 hour) |
| **Public Free Resources** | Low | In-Memory (Caffeine) or Redis | `resources::published` (TTL: 1 hour) |

*Note: User progress, payment verification, and video playback OTPs must remain dynamic and un-cached to ensure strict access control and real-time resumption.*

---

## 6. Architecture Evolution Path

```
Phase 1: Current Architecture (Validated & Production Ready)
┌────────────────────────────────────────────────────────────────────────┐
│  Stateless Modular Monolith (Spring Boot 3.4 / Java 21)                │
│  + MySQL 8 Relational Ledger + VdoCipher CDN + Razorpay Gateway       │
│  Capacity: Handles thousands of concurrent students cost-effectively.   │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
Phase 2: Scale-Out (High Traffic Growth)
┌────────────────────────────────────────────────────────────────────────┐
│  Load Balancer (AWS ALB / Cloudflare WAF)                              │
│  + Multiple Spring Boot Nodes (Horizontally Scaled)                    │
│  + Managed MySQL (Primary Read/Write + Read Replicas)                  │
│  + Redis Cache-Aside for Course Catalog & Distributed Rate Limiting    │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
Phase 3: High-Throughput Event-Driven Scale (Millions of Events)
┌────────────────────────────────────────────────────────────────────────┐
│  Message Broker (Kafka / RabbitMQ) for asynchronous progress events    │
│  and transactional email notifications                                │
│  + Optional service decomposition (Auth / Payment / Content services) │
└────────────────────────────────────────────────────────────────────────┘
```
