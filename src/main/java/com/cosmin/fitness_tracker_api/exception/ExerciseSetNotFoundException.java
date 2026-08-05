package com.cosmin.fitness_tracker_api.exception;

public class ExerciseSetNotFoundException extends RuntimeException {
    public ExerciseSetNotFoundException(String message) {
        super(message);
    }
}
