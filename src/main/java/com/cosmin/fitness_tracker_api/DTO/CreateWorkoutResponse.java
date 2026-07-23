package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;
import java.util.List;

public record CreateWorkoutResponse(
        Long id,
        String workoutName,
        LocalDate date,
        List<WorkoutExerciseResponse> exerciseResponses,
        int goalsCompleted
) {
}
