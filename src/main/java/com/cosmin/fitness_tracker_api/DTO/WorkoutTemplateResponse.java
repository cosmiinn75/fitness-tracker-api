package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;
import java.util.List;

public record WorkoutTemplateResponse(
        Long id,
        String workoutName,
        LocalDate createdAt,
        List<WorkoutTemplateExerciseResponse> templateExercises
) {
}
