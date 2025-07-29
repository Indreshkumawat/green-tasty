package com.restaurantapp.exception;

public class FeedbackAlreadyExistException extends RuntimeException {
    public FeedbackAlreadyExistException(String message) {
        super(message);
    }
}
