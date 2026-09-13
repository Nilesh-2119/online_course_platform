package com.courseplatform.auth.dto;

import com.courseplatform.auth.otp.OtpChannel;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyOtpResponse {

    private OtpChannel channel;
    private boolean verified;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean accountActivated;
    private String message;
    private AuthResponse auth;

    public VerifyOtpResponse() {
    }

    public VerifyOtpResponse(OtpChannel channel, boolean verified, boolean emailVerified, boolean phoneVerified, boolean accountActivated, String message, AuthResponse auth) {
        this.channel = channel;
        this.verified = verified;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.accountActivated = accountActivated;
        this.message = message;
        this.auth = auth;
    }

    public OtpChannel getChannel() {
        return channel;
    }

    public void setChannel(OtpChannel channel) {
        this.channel = channel;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isAccountActivated() {
        return accountActivated;
    }

    public void setAccountActivated(boolean accountActivated) {
        this.accountActivated = accountActivated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuthResponse getAuth() {
        return auth;
    }

    public void setAuth(AuthResponse auth) {
        this.auth = auth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OtpChannel channel;
        private boolean verified;
        private boolean emailVerified;
        private boolean phoneVerified;
        private boolean accountActivated;
        private String message;
        private AuthResponse auth;

        public Builder channel(OtpChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder phoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
            return this;
        }

        public Builder accountActivated(boolean accountActivated) {
            this.accountActivated = accountActivated;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder auth(AuthResponse auth) {
            this.auth = auth;
            return this;
        }

        public VerifyOtpResponse build() {
            return new VerifyOtpResponse(channel, verified, emailVerified, phoneVerified, accountActivated, message, auth);
        }
    }
}
