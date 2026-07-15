package com.cosmin.fitness_tracker_api.Exception;

public class ExerciseSetNotFoundException extends RuntimeException {
    public ExerciseSetNotFoundException(String message) {
        super(message);
    }
}
