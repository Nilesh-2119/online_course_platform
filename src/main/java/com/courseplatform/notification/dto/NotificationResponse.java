package com.courseplatform.notification.dto;

import com.courseplatform.notification.NotificationEntity;

import java.time.Instant;

public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String tag;
    private String linkUrl;
    private boolean active;
    private boolean isNew;
    private Instant createdAt;
    private Instant updatedAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, String title, String message, String tag, String linkUrl, boolean active, boolean isNew, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.tag = tag;
        this.linkUrl = linkUrl;
        this.active = active;
        this.isNew = isNew;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationResponse fromEntity(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new NotificationResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getTag(),
                entity.getLinkUrl(),
                entity.isActive(),
                entity.isNew(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
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
