package com.cosmin.fitness_tracker_api.Exception;

public class WorkoutExerciseNotFoundException extends RuntimeException {
    public WorkoutExerciseNotFoundException(String message) {
        super(message);
    }
}
