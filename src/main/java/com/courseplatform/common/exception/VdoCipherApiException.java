package com.courseplatform.common.exception;

public class VdoCipherApiException extends RuntimeException {
    public VdoCipherApiException(String message) {
        super(message);
    }

    public VdoCipherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
