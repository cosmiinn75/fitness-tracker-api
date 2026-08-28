package com.cosmin.fitness_tracker_api.DTO;

import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;

import java.io.Serializable;

public record ExerciseDefinitionResponse(
        Long id,
        String exerciseName,
        MuscleGroup muscleGroup,
        ExerciseType exerciseType,
        boolean archived
) implements Serializable {
}
