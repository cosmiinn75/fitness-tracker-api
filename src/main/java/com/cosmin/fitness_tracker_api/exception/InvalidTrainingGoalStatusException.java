package com.cosmin.fitness_tracker_api.exception;

public class InvalidTrainingGoalStatusException extends RuntimeException {
    public InvalidTrainingGoalStatusException(String message) {
        super(message);
    }
}
