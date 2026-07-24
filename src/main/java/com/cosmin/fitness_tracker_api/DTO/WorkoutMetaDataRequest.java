package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record WorkoutMetaDataRequest(
        @Size(min = 3 , max = 50)
        String workoutName,

        @PastOrPresent
        LocalDate date
) {
}