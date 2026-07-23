package com.cosmin.fitness_tracker_api.Exception;

public class ActiveTrainingGoalAlreadyExistsException extends RuntimeException {
    public ActiveTrainingGoalAlreadyExistsException(String message) {
        super(message);
    }
}
