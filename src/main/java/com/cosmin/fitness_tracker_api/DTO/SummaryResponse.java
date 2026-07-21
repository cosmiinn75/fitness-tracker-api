package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;

public record SummaryResponse(
        long totalWorkouts,
        long trainingDaysLast7Days,
        long trainingDaysLast30Days,
        long totalSetsLast7Days,
        LocalDate lastWorkoutDate,
        String mostTrainedExerciseLast30Days
)
{
}
