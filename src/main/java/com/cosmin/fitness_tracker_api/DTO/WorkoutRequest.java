package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record WorkoutRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        @NotNull
        String workoutName,


        @PastOrPresent
        @NotNull
        LocalDate date,

        @NotEmpty
        @NotNull
        List<@Valid WorkoutExerciseRequest> exerciseRequests
) {
}
