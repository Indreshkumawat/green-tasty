package com.restaurantapp.exception;

public class ReservationCancellationOrModificationException extends Exception {
    public ReservationCancellationOrModificationException(String message) {
        super(message);
    }
}
