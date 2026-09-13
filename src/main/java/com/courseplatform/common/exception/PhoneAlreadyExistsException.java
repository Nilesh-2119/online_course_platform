package com.courseplatform.common.exception;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String phone) {
        super("An account with phone number '" + phone + "' already exists. Please use a different phone number or log in.");
    }
}
