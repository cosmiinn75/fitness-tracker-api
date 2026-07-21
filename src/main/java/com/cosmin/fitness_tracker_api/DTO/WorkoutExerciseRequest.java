package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record WorkoutExerciseRequest(

        @Positive
        @NotNull
        Long exerciseDefinitionId,

        @NotEmpty
        List<@Valid SetRequest> setRequests
) {
}
