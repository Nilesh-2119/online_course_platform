package com.courseplatform.common.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super(String.format("An account with email '%s' already exists", email));
    }
}
