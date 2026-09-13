package com.courseplatform.course.dto;

import com.courseplatform.course.CourseStatus;

import java.math.BigDecimal;

public class CourseSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private CourseStatus status;
    private int totalSections;
    private int totalVideos;

    public CourseSummaryResponse() {
    }

    public CourseSummaryResponse(Long id, String title, String description, BigDecimal price, String currency, CourseStatus status, int totalSections, int totalVideos) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.totalSections = totalSections;
        this.totalVideos = totalVideos;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public int getTotalSections() {
        return totalSections;
    }

    public void setTotalSections(int totalSections) {
        this.totalSections = totalSections;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private BigDecimal price;
        private String currency;
        private CourseStatus status;
        private int totalSections;
        private int totalVideos;

        public Builder id(Long id) {
            this.id = id;
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

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(CourseStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalSections(int totalSections) {
            this.totalSections = totalSections;
            return this;
        }

        public Builder totalVideos(int totalVideos) {
            this.totalVideos = totalVideos;
            return this;
        }

        public CourseSummaryResponse build() {
            return new CourseSummaryResponse(id, title, description, price, currency, status, totalSections, totalVideos);
        }
    }
}
