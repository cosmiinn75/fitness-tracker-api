package com.cosmin.fitness_tracker_api.exception;

public class InvalidBodyException extends RuntimeException {
    public InvalidBodyException(String message) {
        super(message);
    }
}
