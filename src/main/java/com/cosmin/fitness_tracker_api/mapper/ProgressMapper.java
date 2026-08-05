package com.cosmin.fitness_tracker_api.mapper;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.model.ExerciseSet;
import com.cosmin.fitness_tracker_api.model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.repository.Projection.PersonalRecordProjection;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ProgressMapper {

    public WorkoutExerciseHistoryResponse
    toWorkoutExerciseHistoryResponse(
            WorkoutExercise exercise,
            List<SetResponse> setResponses,
            double estimatedOneRepMax
    ) {
        return new WorkoutExerciseHistoryResponse(
                exercise.getWorkout().getId(),
                exercise.getId(),
                exercise.getExerciseNumber(),
                exercise.getExerciseDefinition().getName(),
                estimatedOneRepMax,
                exercise.getWorkout().getDate(),
                setResponses
        );
    }

    public SummaryResponse toSummaryResponse(
            long totalWorkouts,
            long trainingDaysLast7Days,
            long trainingDaysLast30Days,
            long totalSetsLast7Days,
            LocalDate lastWorkoutDate,
            String mostTrainedExercise
    ) {
        return new SummaryResponse(
                totalWorkouts,
                trainingDaysLast7Days,
                trainingDaysLast30Days,
                totalSetsLast7Days,
                lastWorkoutDate,
                mostTrainedExercise
        );
    }

    public WorkoutVolumeResponse toWorkoutVolumeResponse(
            double value
    ) {
        return new WorkoutVolumeResponse(value);
    }

    public VolumeProgressResponse toVolumeProgressResponse(
            LocalDate start,
            LocalDate end,
            Double totalVolume
    ) {
        return new VolumeProgressResponse(
                start,
                end,
                totalVolume
        );
    }

    public PersonalRecordResponse toPersonalRecordResponse(
            ExerciseDefinition exerciseDefinition,
            ExerciseSet exerciseSet
    ) {
        return new PersonalRecordResponse(
                exerciseDefinition.getId(),
                exerciseDefinition.getName(),
                exerciseSet.getWeight(),
                exerciseSet.getReps(),
                exerciseSet.getRir(),
                exerciseSet.getWorkoutExercise()
                        .getWorkout()
                        .getDate()
        );
    }

    public PersonalRecordResponse toPersonalRecordResponse(
            PersonalRecordProjection projection
    ) {
        return new PersonalRecordResponse(
                projection.getExerciseDefinitionId(),
                projection.getExerciseName(),
                projection.getWeight(),
                projection.getReps(),
                projection.getRir(),
                projection.getWorkoutDate()
        );
    }
}