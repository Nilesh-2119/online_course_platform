package com.courseplatform.payment.dto;

public class VerifyPaymentResponse {

    private boolean verified;
    private Long courseId;
    private String courseTitle;
    private String message;
    private String razorpayPaymentId;

    public VerifyPaymentResponse() {
    }

    public VerifyPaymentResponse(boolean verified, Long courseId, String courseTitle, String message, String razorpayPaymentId) {
        this.verified = verified;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.message = message;
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean verified;
        private Long courseId;
        private String courseTitle;
        private String message;
        private String razorpayPaymentId;

        public Builder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public Builder courseId(Long courseId) {
            this.courseId = courseId;
            return this;
        }

        public Builder courseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder razorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
            return this;
        }

        public VerifyPaymentResponse build() {
            return new VerifyPaymentResponse(verified, courseId, courseTitle, message, razorpayPaymentId);
        }
    }
}
