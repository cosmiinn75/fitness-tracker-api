package com.cosmin.fitness_tracker_api.DTO;

import com.cosmin.fitness_tracker_api.Enum.Status;

import java.time.LocalDate;

public record TrainingGoalResponse(
        Long id,
        String exerciseName,
        Double targetWeight,
        Integer targetReps,
        LocalDate targetDate,
        Status status
) {
}
