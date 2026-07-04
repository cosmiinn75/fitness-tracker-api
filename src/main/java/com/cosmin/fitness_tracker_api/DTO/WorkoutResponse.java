package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;
import java.util.List;

public record WorkoutResponse(
        Long id,
        String workoutName,
        LocalDate date,
        List<ExerciseResponse> exerciseResponses
) {
}
