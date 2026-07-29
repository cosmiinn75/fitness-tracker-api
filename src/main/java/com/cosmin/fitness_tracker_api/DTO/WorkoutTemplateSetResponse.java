package com.cosmin.fitness_tracker_api.DTO;

public record WorkoutTemplateSetResponse(
        Long id,
        Integer setNumber,
        Double targetWeight,
        Integer targetReps,
        Integer targetRir
) {
}
