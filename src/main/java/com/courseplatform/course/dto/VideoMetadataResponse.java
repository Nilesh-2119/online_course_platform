package com.courseplatform.course.dto;

import com.courseplatform.course.VideoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoMetadataResponse {

    private Long id;
    private Long sectionId;
    private String title;
    private String description;
    private Integer durationSeconds;
    private Integer displayOrder;

    @JsonProperty("isFree")
    private boolean isFree;

    private boolean locked;
    private VideoStatus status;

    public VideoMetadataResponse() {
    }

    public VideoMetadataResponse(Long id, Long sectionId, String title, String description, Integer durationSeconds, Integer displayOrder, boolean isFree, boolean locked, VideoStatus status) {
        this.id = id;
        this.sectionId = sectionId;
        this.title = title;
        this.description = description;
        this.durationSeconds = durationSeconds;
        this.displayOrder = displayOrder;
        this.isFree = isFree;
        this.locked = locked;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long sectionId;
        private String title;
        private String description;
        private Integer durationSeconds;
        private Integer displayOrder;
        private boolean isFree;
        private boolean locked;
        private VideoStatus status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder sectionId(Long sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Builder isFree(boolean isFree) {
            this.isFree = isFree;
            return this;
        }

        public Builder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public Builder status(VideoStatus status) {
            this.status = status;
            return this;
        }

        public VideoMetadataResponse build() {
            return new VideoMetadataResponse(id, sectionId, title, description, durationSeconds, displayOrder, isFree, locked, status);
        }
    }
}
