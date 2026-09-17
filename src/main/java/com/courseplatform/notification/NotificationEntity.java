package com.courseplatform.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entity representing an announcement / notification shown to students on their dashboard.
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_active_created", columnList = "active, created_at")
        }
)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "tag", nullable = false, length = 50)
    private String tag = "ANNOUNCEMENT";

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "is_new", nullable = false)
    private boolean isNew = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public NotificationEntity() {
    }

    public NotificationEntity(String title, String message, String tag, String linkUrl, boolean active, boolean isNew) {
        this.title = title;
        this.message = message;
        this.tag = (tag != null && !tag.trim().isEmpty()) ? tag.trim().toUpperCase() : "ANNOUNCEMENT";
        this.linkUrl = (linkUrl != null && !linkUrl.trim().isEmpty()) ? linkUrl.trim() : null;
        this.active = active;
        this.isNew = isNew;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        if (this.tag == null || this.tag.trim().isEmpty()) {
            this.tag = "ANNOUNCEMENT";
        } else {
            this.tag = this.tag.trim().toUpperCase();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
