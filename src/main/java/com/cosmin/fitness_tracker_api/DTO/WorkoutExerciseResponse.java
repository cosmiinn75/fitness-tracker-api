package com.cosmin.fitness_tracker_api.DTO;

import java.util.List;

public record WorkoutExerciseResponse(
        Long id,
        Integer exerciseNumber,
        String exerciseName,
        List<SetResponse> setResponses
) {
}
