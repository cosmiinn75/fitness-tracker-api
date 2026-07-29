package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;

public record WorkoutTemplateSetRequest(

        @Positive
        Double targetWeight,


        @NotNull
        @Min(1)
        @Max(100)
        Integer targetReps,


        @Min(0)
        @Max(5)
        Integer targetRir
) {
}
