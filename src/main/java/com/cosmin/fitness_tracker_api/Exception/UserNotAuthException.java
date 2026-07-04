package com.cosmin.fitness_tracker_api.Exception;

public class UserNotAuthException extends RuntimeException {
    public UserNotAuthException(String message) {
        super(message);
    }
}
