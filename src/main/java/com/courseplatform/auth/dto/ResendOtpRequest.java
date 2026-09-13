package com.courseplatform.auth.dto;

import com.courseplatform.auth.otp.OtpChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResendOtpRequest {

    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;

    @NotNull(message = "Verification channel is required (EMAIL or PHONE)")
    private OtpChannel channel;

    public ResendOtpRequest() {
    }

    public ResendOtpRequest(String identifier, OtpChannel channel) {
        this.identifier = identifier;
        this.channel = channel;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String identifier;
        private OtpChannel channel;

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder channel(OtpChannel channel) {
            this.channel = channel;
            return this;
        }

        public ResendOtpRequest build() {
            return new ResendOtpRequest(identifier, channel);
        }
    }
}
