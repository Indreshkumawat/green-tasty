package com.restaurantapp.exception;

public class UserDoesNotExistsException extends Exception {
    public UserDoesNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDoesNotExistsException() {
    }

    public UserDoesNotExistsException(String message) {
        super(message);
    }

    public UserDoesNotExistsException(Throwable cause) {
        super(cause);
    }

    public UserDoesNotExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
