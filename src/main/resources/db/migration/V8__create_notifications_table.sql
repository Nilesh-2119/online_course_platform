-- V8: Create notifications table for admin announcements and user dashboard notifications

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    tag VARCHAR(50) NOT NULL DEFAULT 'ANNOUNCEMENT',
    link_url VARCHAR(500) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_new BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notifications_active_created (active, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
