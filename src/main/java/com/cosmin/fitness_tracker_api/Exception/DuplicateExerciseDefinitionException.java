package com.cosmin.fitness_tracker_api.Exception;

public class DuplicateExerciseDefinitionException extends RuntimeException {
    public DuplicateExerciseDefinitionException(String message) {
        super(message);
    }
}
