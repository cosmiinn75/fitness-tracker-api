package com.cosmin.fitness_tracker_api.Exception;

public class WorkoutTemplateNotFoundException extends RuntimeException {
    public WorkoutTemplateNotFoundException(String message) {
        super(message);
    }
}
