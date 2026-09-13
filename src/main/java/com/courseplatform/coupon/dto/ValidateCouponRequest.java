package com.courseplatform.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for validating a coupon code at checkout.
 */
public class ValidateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    public ValidateCouponRequest() {
    }

    public ValidateCouponRequest(String code, Long courseId) {
        this.code = code;
        this.courseId = courseId;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}
