package com.courseplatform.progress.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class VideoProgressResponse {

    private Long videoId;
    private Integer lastPositionSeconds;

    @JsonProperty("completed")
    private boolean completed;

    private Instant updatedAt;

    public VideoProgressResponse() {
    }

    public VideoProgressResponse(Long videoId, Integer lastPositionSeconds, boolean completed, Instant updatedAt) {
        this.videoId = videoId;
        this.lastPositionSeconds = lastPositionSeconds;
        this.completed = completed;
        this.updatedAt = updatedAt;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Integer getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds) {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long videoId;
        private Integer lastPositionSeconds;
        private boolean completed;
        private Instant updatedAt;

        public Builder videoId(Long videoId) {
            this.videoId = videoId;
            return this;
        }

        public Builder lastPositionSeconds(Integer lastPositionSeconds) {
            this.lastPositionSeconds = lastPositionSeconds;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public VideoProgressResponse build() {
            return new VideoProgressResponse(videoId, lastPositionSeconds, completed, updatedAt);
        }
    }
}
