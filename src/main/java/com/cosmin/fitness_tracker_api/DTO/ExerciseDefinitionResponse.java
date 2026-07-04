package com.cosmin.fitness_tracker_api.DTO;

import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;

public record ExerciseDefinitionResponse(
        Long id,
        String exerciseName,
        MuscleGroup muscleGroup
) {
}
