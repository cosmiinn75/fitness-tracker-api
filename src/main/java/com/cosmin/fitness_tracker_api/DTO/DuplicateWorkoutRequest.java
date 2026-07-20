package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DuplicateWorkoutRequest(
        @NotNull LocalDate date,
        @Size(min = 2 , max = 50) String workoutName
        ) {
}
