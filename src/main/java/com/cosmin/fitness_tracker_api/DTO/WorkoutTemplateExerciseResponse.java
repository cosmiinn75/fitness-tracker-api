package com.cosmin.fitness_tracker_api.DTO;

import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;

import java.util.List;

public record WorkoutTemplateExerciseResponse(
        Long id,
        Long exerciseDefinitionId,
        Integer exerciseNumber,
        MuscleGroup muscleGroup,
        String exerciseName,
        List<WorkoutTemplateSetResponse> templateSets
) {
}
