package com.cosmin.fitness_tracker_api.Exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
