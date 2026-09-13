package com.courseplatform.course.dto;

import com.courseplatform.course.CourseStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class CourseDetailResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private CourseStatus status;

    @JsonProperty("isPurchased")
    private boolean isPurchased;

    private List<CourseSectionResponse> sections;

    public CourseDetailResponse() {
    }

    public CourseDetailResponse(Long id, String title, String description, BigDecimal price, String currency, CourseStatus status, boolean isPurchased, List<CourseSectionResponse> sections) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.isPurchased = isPurchased;
        this.sections = sections;
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

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public List<CourseSectionResponse> getSections() {
        return sections;
    }

    public void setSections(List<CourseSectionResponse> sections) {
        this.sections = sections;
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
        private boolean isPurchased;
        private List<CourseSectionResponse> sections;

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

        public Builder isPurchased(boolean isPurchased) {
            this.isPurchased = isPurchased;
            return this;
        }

        public Builder sections(List<CourseSectionResponse> sections) {
            this.sections = sections;
            return this;
        }

        public CourseDetailResponse build() {
            return new CourseDetailResponse(id, title, description, price, currency, status, isPurchased, sections);
        }
    }
}
