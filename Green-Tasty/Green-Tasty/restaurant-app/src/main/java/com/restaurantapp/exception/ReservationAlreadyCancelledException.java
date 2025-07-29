package com.restaurantapp.exception;

public class ReservationAlreadyCancelledException extends Exception {
    public ReservationAlreadyCancelledException(String message) {
        super(message);
    }
}
