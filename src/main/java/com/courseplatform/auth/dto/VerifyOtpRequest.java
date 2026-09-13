package com.courseplatform.auth.dto;

import com.courseplatform.auth.otp.OtpChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {

    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;

    @NotNull(message = "Verification channel is required (EMAIL or PHONE)")
    private OtpChannel channel;

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{4,8}$", message = "Verification code must be numeric between 4 and 8 digits")
    private String otp;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String identifier, OtpChannel channel, String otp) {
        this.identifier = identifier;
        this.channel = channel;
        this.otp = otp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public OtpChannel getChannel() {
        return channel;
    }

    public void setChannel(OtpChannel channel) {
        this.channel = channel;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String identifier;
        private OtpChannel channel;
        private String otp;

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder channel(OtpChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder otp(String otp) {
            this.otp = otp;
            return this;
        }

        public VerifyOtpRequest build() {
            return new VerifyOtpRequest(identifier, channel, otp);
        }
    }
}
