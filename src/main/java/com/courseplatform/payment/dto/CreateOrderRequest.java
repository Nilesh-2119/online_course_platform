package com.courseplatform.payment.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String couponCode;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long courseId) {
        this.courseId = courseId;
    }

    public CreateOrderRequest(Long courseId, String couponCode) {
        this.courseId = courseId;
        this.couponCode = couponCode;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long courseId;
        private String couponCode;

        public Builder courseId(Long courseId) {
            this.courseId = courseId;
            return this;
        }

        public Builder couponCode(String couponCode) {
            this.couponCode = couponCode;
            return this;
        }

        public CreateOrderRequest build() {
            return new CreateOrderRequest(courseId, couponCode);
        }
    }
}
