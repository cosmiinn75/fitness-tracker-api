package com.cosmin.fitness_tracker_api.Exception;

public class TrainingGoalNotFoundException extends RuntimeException {
    public TrainingGoalNotFoundException(String message) {
        super(message);
    }
}
