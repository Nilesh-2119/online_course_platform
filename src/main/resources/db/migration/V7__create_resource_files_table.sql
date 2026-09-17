-- =============================================================================
-- V7: Resource Files Table (Permanent Database Storage for Uploaded Free Resources)
-- =============================================================================

CREATE TABLE IF NOT EXISTS resource_files (
    file_name VARCHAR(255) PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NULL,
    file_size BIGINT NOT NULL,
    file_data LONGBLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
