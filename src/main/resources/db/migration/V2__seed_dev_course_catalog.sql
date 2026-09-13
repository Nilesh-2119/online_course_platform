-- =============================================================================
-- SEED DATA: Course Catalog & Free Resources
-- Version: 2.0 (Seed Course, 3 Sections, Videos, and Free Resources)
-- Note: Contains NO payment or user transaction records.
-- =============================================================================

-- Seed Course (Price: ₹4,000.00 INR)
INSERT INTO courses (id, title, description, price, currency, status, created_at, updated_at)
VALUES (
    1,
    'Enterprise Backend Architecture & Security',
    'Comprehensive masterclass on building secure, production-ready, horizontally scalable backend systems with Spring Boot, MySQL, Razorpay, and VdoCipher.',
    4000.00,
    'INR',
    'PUBLISHED',
    NOW(),
    NOW()
);

-- Seed 3 Course Sections
INSERT INTO course_sections (id, course_id, title, description, display_order, created_at, updated_at)
VALUES 
(
    1,
    1,
    'Section 1: Architecture, Security Foundation & Database Design',
    'Core architectural foundations, relational database constraints, stateless security, and clean API design.',
    1,
    NOW(),
    NOW()
),
(
    2,
    1,
    'Section 2: Payment Integration & Entitlement Engine',
    'End-to-end Razorpay integration, server-side truth, idempotent webhook processing, and transaction state management.',
    2,
    NOW(),
    NOW()
),
(
    3,
    1,
    'Section 3: DRM Video Streaming & Scalability',
    'VdoCipher OTP generation, dynamic watermarking, progress tracking, and horizontal scalability under load.',
    3,
    NOW(),
    NOW()
);

-- Seed Videos (Free preview in Section 1, Paid videos across sections)
INSERT INTO videos (id, section_id, title, description, vdocipher_video_id, duration_seconds, display_order, is_free, status, created_at, updated_at)
VALUES 
-- Section 1 Videos
(
    1,
    1,
    'Course Overview & Architecture Blueprint',
    'High-level walkthrough of the platform architecture, security principles, and technology stack.',
    '9a8898197d5743b7ac095b97a4ae0536',
    600,
    1,
    TRUE,
    'PUBLISHED',
    NOW(),
    NOW()
),
(
    2,
    1,
    'Database Normalization & Schema Constraints',
    'Designing relational models with strict constraints, indexes, and Flyway versioning.',
    '9a8898197d5743b7ac095b97a4ae0536',
    1200,
    2,
    FALSE,
    'PUBLISHED',
    NOW(),
    NOW()
),
-- Section 2 Videos
(
    3,
    2,
    'Razorpay Order Lifecycle & Server-Side Security',
    'Implementing secure order creation and preventing client-side price tampering.',
    'vdo_paid_sec2_01',
    1500,
    1,
    FALSE,
    'PUBLISHED',
    NOW(),
    NOW()
),
(
    4,
    2,
    'Idempotent Webhook Verification & Course Access',
    'Handling webhooks, verifying HMAC SHA256 signatures, and granting course access safely.',
    'vdo_paid_sec2_02',
    1800,
    2,
    FALSE,
    'PUBLISHED',
    NOW(),
    NOW()
),
-- Section 3 Videos
(
    5,
    3,
    'VdoCipher Server-Side OTP & Player Integration',
    'Protecting video assets with server-generated OTPs and playbackInfo.',
    'vdo_paid_sec3_01',
    1400,
    1,
    FALSE,
    'PUBLISHED',
    NOW(),
    NOW()
),
(
    6,
    3,
    'Playback Progress Tracking & Resume Functionality',
    'Tracking student video completion and resuming playback across devices.',
    'vdo_paid_sec3_02',
    1100,
    2,
    FALSE,
    'PUBLISHED',
    NOW(),
    NOW()
);

-- Seed Free Resources
INSERT INTO free_resources (id, title, description, resource_type, resource_url, status, created_at, updated_at)
VALUES 
(
    1,
    'Backend System Architecture Checklist',
    'A comprehensive checklist for reviewing application security, database indexes, and payment idempotency.',
    'PDF',
    'https://resources.courseplatform.com/docs/backend-architecture-checklist.pdf',
    'PUBLISHED',
    NOW(),
    NOW()
),
(
    2,
    'Database Schema Diagram & Indexing Guide',
    'Visual schema diagram and query indexing cheat sheet for MySQL 8+.',
    'PDF',
    'https://resources.courseplatform.com/docs/schema-indexing-guide.pdf',
    'PUBLISHED',
    NOW(),
    NOW()
);
