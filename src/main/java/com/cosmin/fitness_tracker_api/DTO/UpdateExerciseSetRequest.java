package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;

public record UpdateExerciseSetRequest(
        @PositiveOrZero(message = "Weight cannot be negative")
        Double weight,

        @Min(value = 1, message = "Reps must be at least 1")
        @Max(value = 100, message = "Reps cannot exceed 100")
        Integer reps,

        @Min(value = 0, message = "RIR cannot be negative")
        @Max(value = 5, message = "RIR cannot be higher than 5")
        Integer rir
) {
}
