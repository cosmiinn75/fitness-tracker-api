package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;

public record UpdateExerciseSetRequest(

        @Positive(message = "Weight must be positive")
        Double weight,

        @Positive(message = "Reps must be positive")
        Integer reps,

        @Min(value = 0, message = "RIR cannot be negative")
        @Max(value = 5 , message = "RIR cannot be higher than 5")
        Integer rir
) {
}
