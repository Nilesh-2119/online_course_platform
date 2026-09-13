package com.courseplatform.progress.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SectionProgressResponse {

    private Long sectionId;
    private String sectionTitle;
    private int totalVideos;
    private int completedVideos;

    @JsonProperty("completed")
    private boolean completed;

    public SectionProgressResponse() {
    }

    public SectionProgressResponse(Long sectionId, String sectionTitle, int totalVideos, int completedVideos, boolean completed) {
        this.sectionId = sectionId;
        this.sectionTitle = sectionTitle;
        this.totalVideos = totalVideos;
        this.completedVideos = completedVideos;
        this.completed = completed;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public int getCompletedVideos() {
        return completedVideos;
    }

    public void setCompletedVideos(int completedVideos) {
        this.completedVideos = completedVideos;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long sectionId;
        private String sectionTitle;
        private int totalVideos;
        private int completedVideos;
        private boolean completed;

        public Builder sectionId(Long sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public Builder sectionTitle(String sectionTitle) {
            this.sectionTitle = sectionTitle;
            return this;
        }

        public Builder totalVideos(int totalVideos) {
            this.totalVideos = totalVideos;
            return this;
        }

        public Builder completedVideos(int completedVideos) {
            this.completedVideos = completedVideos;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public SectionProgressResponse build() {
            return new SectionProgressResponse(sectionId, sectionTitle, totalVideos, completedVideos, completed);
        }
    }
}
