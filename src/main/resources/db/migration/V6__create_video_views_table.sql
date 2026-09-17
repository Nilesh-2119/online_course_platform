-- =============================================================================
-- V6: Video Views Tracking Table (Landing Page VSL & Dashboard Welcome Video)
-- =============================================================================

CREATE TABLE video_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_type VARCHAR(50) NOT NULL,
    video_id VARCHAR(100) NULL,
    user_id BIGINT NULL,
    ip_address VARCHAR(64) NULL,
    user_agent VARCHAR(512) NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_views_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_video_views_type ON video_views(video_type);
CREATE INDEX idx_video_views_viewed_at ON video_views(viewed_at);
CREATE INDEX idx_video_views_user ON video_views(user_id);
CREATE INDEX idx_video_views_type_viewed_at ON video_views(video_type, viewed_at);
