package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainingGoalRequest(
        @NotNull
        @Positive
        Long exerciseDefinitionId,

        @Positive
        @NotNull
        Double targetWeight,

        @Positive
        @NotNull
        Integer targetReps,

        @NotNull
        @Future
        LocalDate targetDate
) {
}
