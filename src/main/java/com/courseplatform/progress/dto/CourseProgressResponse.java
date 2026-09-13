package com.courseplatform.progress.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CourseProgressResponse {

    private Long courseId;
    private String courseTitle;
    private int totalVideos;
    private int completedVideos;
    private int percentCompleted;

    @JsonProperty("completed")
    private boolean completed;

    private List<SectionProgressResponse> sections;

    public CourseProgressResponse() {
    }

    public CourseProgressResponse(Long courseId, String courseTitle, int totalVideos, int completedVideos, int percentCompleted, boolean completed, List<SectionProgressResponse> sections) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.totalVideos = totalVideos;
        this.completedVideos = completedVideos;
        this.percentCompleted = percentCompleted;
        this.completed = completed;
        this.sections = sections;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
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

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(int percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<SectionProgressResponse> getSections() {
        return sections;
    }

    public void setSections(List<SectionProgressResponse> sections) {
        this.sections = sections;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long courseId;
        private String courseTitle;
        private int totalVideos;
        private int completedVideos;
        private int percentCompleted;
        private boolean completed;
        private List<SectionProgressResponse> sections;

        public Builder courseId(Long courseId) {
            this.courseId = courseId;
            return this;
        }

        public Builder courseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
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

        public Builder percentCompleted(int percentCompleted) {
            this.percentCompleted = percentCompleted;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public Builder sections(List<SectionProgressResponse> sections) {
            this.sections = sections;
            return this;
        }

        public CourseProgressResponse build() {
            return new CourseProgressResponse(courseId, courseTitle, totalVideos, completedVideos, percentCompleted, completed, sections);
        }
    }
}
