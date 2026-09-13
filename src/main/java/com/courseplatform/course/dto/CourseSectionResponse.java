package com.courseplatform.course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseSectionResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer displayOrder;
    private int totalVideos;
    private List<VideoMetadataResponse> videos;

    public CourseSectionResponse() {
    }

    public CourseSectionResponse(Long id, Long courseId, String title, String description, Integer displayOrder, int totalVideos, List<VideoMetadataResponse> videos) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.displayOrder = displayOrder;
        this.totalVideos = totalVideos;
        this.videos = videos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public List<VideoMetadataResponse> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoMetadataResponse> videos) {
        this.videos = videos;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long courseId;
        private String title;
        private String description;
        private Integer displayOrder;
        private int totalVideos;
        private List<VideoMetadataResponse> videos;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder courseId(Long courseId) {
            this.courseId = courseId;
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

        public Builder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Builder totalVideos(int totalVideos) {
            this.totalVideos = totalVideos;
            return this;
        }

        public Builder videos(List<VideoMetadataResponse> videos) {
            this.videos = videos;
            return this;
        }

        public CourseSectionResponse build() {
            return new CourseSectionResponse(id, courseId, title, description, displayOrder, totalVideos, videos);
        }
    }
}
