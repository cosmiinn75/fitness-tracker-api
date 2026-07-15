package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeWorkoutExerciseRequest(
        @NotNull
        @Positive
        Long exerciseDefinitionId
) {
}
