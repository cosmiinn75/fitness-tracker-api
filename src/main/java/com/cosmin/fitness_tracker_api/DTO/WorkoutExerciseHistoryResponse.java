package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;
import java.util.List;

public record WorkoutExerciseHistoryResponse(
        Long workoutId,
        Long workoutExerciseId,
        Integer exerciseNumber,
        String exerciseName,
        Double estimatedOneRepMax,
        LocalDate workoutDate,
        List<SetResponse> setResponses

) {
}
