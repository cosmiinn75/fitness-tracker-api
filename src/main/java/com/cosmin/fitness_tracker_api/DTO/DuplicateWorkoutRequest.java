package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DuplicateWorkoutRequest(
        @NotNull
        @PastOrPresent
        LocalDate date,
        @Size(min = 2 , max = 50) String workoutName
        ) {
}
