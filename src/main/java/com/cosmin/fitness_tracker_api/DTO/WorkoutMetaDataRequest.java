package com.cosmin.fitness_tracker_api.DTO;

import java.time.LocalDate;

public record WorkoutMetaDataRequest(
        String workoutName,
        LocalDate date
) {
}