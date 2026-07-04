package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;

public record SetRequest(

        @NotNull
        @PositiveOrZero
        Double weight,

        @NotNull
        @Positive
        @Min(1)
        @Max(100)
        Integer reps,


        @Min(0)
        @Max(5)
        Integer rir
) {
}
