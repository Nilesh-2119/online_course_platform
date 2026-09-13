package com.courseplatform.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Common / Generic
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request parameters or body are invalid"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Input validation failed"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported for this endpoint"),
    CONFLICT(HttpStatus.CONFLICT, "A conflict occurred with the current state of the resource"),

    // Security & Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication credentials are required or invalid"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access to this resource is denied"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "The provided token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "The provided token is invalid"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later"),
    ACCOUNT_PENDING_VERIFICATION(HttpStatus.FORBIDDEN, "Account verification is pending. Please complete OTP verification"),
    INVALID_OTP(HttpStatus.BAD_REQUEST, "The verification code is invalid or has already been used"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "The verification code has expired. Please request a new code"),
    OTP_ATTEMPTS_EXHAUSTED(HttpStatus.TOO_MANY_REQUESTS, "Maximum verification attempts exceeded. Please request a new code"),
    OAUTH2_AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "External OAuth authentication failed"),

    // Business domain
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "Payment processing failed"),
    PAYMENT_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "Payment signature verification failed"),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Active enrollment required to access course content"),
    VIDEO_PLAYBACK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate secure video playback credentials");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
