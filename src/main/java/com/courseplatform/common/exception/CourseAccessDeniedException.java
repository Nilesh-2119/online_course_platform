package com.courseplatform.common.exception;

public class CourseAccessDeniedException extends RuntimeException {
    public CourseAccessDeniedException(String message) {
        super(message);
    }
}
