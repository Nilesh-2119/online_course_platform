package com.courseplatform.common.exception;

public class CourseAlreadyPurchasedException extends RuntimeException {
    public CourseAlreadyPurchasedException(String message) {
        super(message);
    }

    public CourseAlreadyPurchasedException(Long courseId) {
        super(String.format("You already own an active subscription or purchase for course ID %d", courseId));
    }
}
