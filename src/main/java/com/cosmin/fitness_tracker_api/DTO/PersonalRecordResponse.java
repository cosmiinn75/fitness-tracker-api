package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;

public record PersonalRecordResponse(
        Long exerciseDefinitionId,
        String exerciseName,
        Double weight,
        Integer reps,
        LocalDate date
) {
}
