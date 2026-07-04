package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkoutRequest(
        @NotNull
        String workoutName,


        LocalDate date,
        List<ExerciseRequest> exerciseRequests
) {
}
