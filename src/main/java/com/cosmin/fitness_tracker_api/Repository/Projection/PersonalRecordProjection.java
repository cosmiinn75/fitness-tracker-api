package com.cosmin.fitness_tracker_api.Repository.Projection;

import java.time.LocalDate;

public interface PersonalRecordProjection {
    Long getExerciseDefinitionId();

    String getExerciseName();

    Double getWeight();

    Integer getReps();

    Integer getRir();

    LocalDate getWorkoutDate();
}
