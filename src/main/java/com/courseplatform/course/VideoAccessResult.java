package com.courseplatform.course;

public class VideoAccessResult {
    private final boolean allowed;
    private final String reason;
    private final VideoEntity video;
    private final boolean freePreview;
    private final boolean enrolled;

    public VideoAccessResult(boolean allowed, String reason, VideoEntity video, boolean freePreview, boolean enrolled) {
        this.allowed = allowed;
        this.reason = reason;
        this.video = video;
        this.freePreview = freePreview;
        this.enrolled = enrolled;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getReason() {
        return reason;
    }

    public VideoEntity getVideo() {
        return video;
    }

    public boolean isFreePreview() {
        return freePreview;
    }

    public boolean isFree() {
        return freePreview;
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public boolean isEntitled() {
        return allowed;
    }

    public static VideoAccessResult allowed(VideoEntity video, boolean freePreview) {
        return new VideoAccessResult(true, null, video, freePreview, !freePreview);
    }

    public static VideoAccessResult denied(String reason) {
        return new VideoAccessResult(false, reason, null, false, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean allowed;
        private String reason;
        private VideoEntity video;
        private boolean freePreview;
        private boolean enrolled;

        public Builder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder video(VideoEntity video) {
            this.video = video;
            return this;
        }

        public Builder freePreview(boolean freePreview) {
            this.freePreview = freePreview;
            return this;
        }

        public Builder enrolled(boolean enrolled) {
            this.enrolled = enrolled;
            return this;
        }

        public VideoAccessResult build() {
            return new VideoAccessResult(allowed, reason, video, freePreview, enrolled);
        }
    }
}
