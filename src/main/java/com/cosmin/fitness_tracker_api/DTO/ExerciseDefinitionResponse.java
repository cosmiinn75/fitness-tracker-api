package com.cosmin.fitness_tracker_api.DTO;

public record ExerciseDefinitionResponse(
        Long id,
        String exerciseName,
        String muscleGroup
) {
}
