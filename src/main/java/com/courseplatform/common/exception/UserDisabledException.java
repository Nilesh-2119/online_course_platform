package com.courseplatform.common.exception;

public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
        super("User account is disabled. Please contact customer support.");
    }
}
