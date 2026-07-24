package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record WorkoutMetaDataRequest(
        @Size(min = 3 , max = 50)
        @Pattern(
                regexp = ".*\\S.*",
                message = "Workout name cannot be blank"
        )
        String workoutName,

        @PastOrPresent
        LocalDate date
) {

        @AssertTrue(message = "At least one field must be provided")
        public boolean isAnyFieldProvided() {
                return workoutName != null || date != null;
        }
}