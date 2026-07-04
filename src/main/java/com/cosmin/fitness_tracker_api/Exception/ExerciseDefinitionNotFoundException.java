package com.cosmin.fitness_tracker_api.Exception;

public class ExerciseDefinitionNotFoundException extends RuntimeException {
    public ExerciseDefinitionNotFoundException(String message) {
        super(message);
    }
}
