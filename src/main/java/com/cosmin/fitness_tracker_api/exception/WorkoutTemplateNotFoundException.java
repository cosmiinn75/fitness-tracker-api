package com.cosmin.fitness_tracker_api.exception;

public class WorkoutTemplateNotFoundException extends RuntimeException {
    public WorkoutTemplateNotFoundException(String message) {
        super(message);
    }
}
