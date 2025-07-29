package com.restaurantapp.exception;

public class WaiterNotAuthorizedException extends Exception {
    public WaiterNotAuthorizedException(String message) {
        super(message);
    }
}

