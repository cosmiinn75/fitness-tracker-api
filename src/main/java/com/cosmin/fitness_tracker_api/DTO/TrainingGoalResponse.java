package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;

public record TrainingGoalResponse(
        String exerciseName,
        Double targetWeight,
        Integer targetReps,
        LocalDate targetDate,
        Long daysLeft
) {
}
