package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record WorkoutRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        String workoutName,


        @PastOrPresent
        LocalDate date,
        @NotEmpty
        List<@Valid ExerciseRequest> exerciseRequests
) {
}
