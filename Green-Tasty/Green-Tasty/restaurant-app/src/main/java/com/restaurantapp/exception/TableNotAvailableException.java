package com.restaurantapp.exception;

public class TableNotAvailableException extends Exception {
    private final String httpMethod;

    public TableNotAvailableException(String msg, String httpMethod) {
        super(msg);
        this.httpMethod = httpMethod;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}
