package com.cosmin.fitness_tracker_api.DTO;

import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExerciseDefinitionRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        String exerciseName,

        @NotNull
        MuscleGroup muscleGroup
) {
}
