package com.restaurantapp.exception;

public class HttpMessageNotReadableException extends IllegalArgumentException {
    public HttpMessageNotReadableException(String message) {
        super(message);
    }
}
