package com.courseplatform.auth.dto;

import com.courseplatform.auth.otp.OtpChannel;

import java.util.List;

public class PendingRegistrationResponse {

    private Long userId;
    private String email;
    private String phone;
    private boolean verificationRequired;
    private List<OtpChannel> requiredChannels;
    private String message;

    public PendingRegistrationResponse() {
    }

    public PendingRegistrationResponse(Long userId, String email, String phone, boolean verificationRequired, List<OtpChannel> requiredChannels, String message) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.verificationRequired = verificationRequired;
        this.requiredChannels = requiredChannels;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }

    public List<OtpChannel> getRequiredChannels() {
        return requiredChannels;
    }

    public void setRequiredChannels(List<OtpChannel> requiredChannels) {
        this.requiredChannels = requiredChannels;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String email;
        private String phone;
        private boolean verificationRequired;
        private List<OtpChannel> requiredChannels;
        private String message;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder verificationRequired(boolean verificationRequired) {
            this.verificationRequired = verificationRequired;
            return this;
        }

        public Builder requiredChannels(List<OtpChannel> requiredChannels) {
            this.requiredChannels = requiredChannels;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public PendingRegistrationResponse build() {
            return new PendingRegistrationResponse(userId, email, phone, verificationRequired, requiredChannels, message);
        }
    }
}
