package com.courseplatform.common.exception;

public class AccountPendingVerificationException extends RuntimeException {

    private final boolean emailVerified;
    private final boolean phoneVerified;

    public AccountPendingVerificationException(boolean emailVerified, boolean phoneVerified) {
        super("Account verification is required before login. Please complete Email and Phone OTP verification.");
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }
}
