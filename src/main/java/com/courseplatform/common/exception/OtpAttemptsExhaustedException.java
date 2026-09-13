package com.courseplatform.common.exception;

public class OtpAttemptsExhaustedException extends RuntimeException {
    public OtpAttemptsExhaustedException(String message) {
        super(message);
    }
}
