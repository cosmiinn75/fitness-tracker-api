package com.cosmin.fitness_tracker_api.exception;

public class DuplicateExerciseDefinitionException extends RuntimeException {
    public DuplicateExerciseDefinitionException(String message) {
        super(message);
    }
}
