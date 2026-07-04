package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SetRequest(

        @PositiveOrZero
        Double weight,

        @Positive
        Integer reps,

        @Min(0)
        @Max(5)
        Integer rir
) {
}
