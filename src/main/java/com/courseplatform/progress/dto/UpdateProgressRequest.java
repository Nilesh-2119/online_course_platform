package com.courseplatform.progress.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateProgressRequest {

    @NotNull(message = "Position in seconds is required")
    @Min(value = 0, message = "Position cannot be negative")
    private Integer lastPositionSeconds;

    private Boolean completed;

    public UpdateProgressRequest() {
    }

    public UpdateProgressRequest(Integer lastPositionSeconds, Boolean completed) {
        this.lastPositionSeconds = lastPositionSeconds;
        this.completed = completed;
    }

    public Integer getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds) {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer lastPositionSeconds;
        private Boolean completed;

        public Builder lastPositionSeconds(Integer lastPositionSeconds) {
            this.lastPositionSeconds = lastPositionSeconds;
            return this;
        }

        public Builder completed(Boolean completed) {
            this.completed = completed;
            return this;
        }

        public UpdateProgressRequest build() {
            return new UpdateProgressRequest(lastPositionSeconds, completed);
        }
    }
}
