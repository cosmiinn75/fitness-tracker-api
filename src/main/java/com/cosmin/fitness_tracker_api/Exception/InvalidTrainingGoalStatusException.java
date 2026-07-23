package com.cosmin.fitness_tracker_api.Exception;

public class InvalidTrainingGoalStatusException extends RuntimeException {
    public InvalidTrainingGoalStatusException(String message) {
        super(message);
    }
}
