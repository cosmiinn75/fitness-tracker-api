package com.cosmin.fitness_tracker_api.mapper;

import com.cosmin.fitness_tracker_api.DTO.CreateWorkoutResponse;
import com.cosmin.fitness_tracker_api.DTO.SetResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutExerciseResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutResponse;
import com.cosmin.fitness_tracker_api.model.ExerciseSet;
import com.cosmin.fitness_tracker_api.model.Workout;
import com.cosmin.fitness_tracker_api.model.WorkoutExercise;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WorkoutMapper {

    public WorkoutResponse toResponse(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getWorkoutName(),
                workout.getDate(),
                toExerciseResponses(workout)
        );
    }

    public CreateWorkoutResponse toCreateResponse(
            Workout workout,
            int goalsCompleted
    ) {
        return new CreateWorkoutResponse(
                workout.getId(),
                workout.getWorkoutName(),
                workout.getDate(),
                toExerciseResponses(workout),
                goalsCompleted
        );
    }

    public WorkoutExerciseResponse toExerciseResponse(
            WorkoutExercise workoutExercise
    ) {
        List<SetResponse> setResponses =
                workoutExercise.getExerciseSets()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        ExerciseSet::getSetNumber
                                )
                        )
                        .map(this::toSetResponse)
                        .toList();

        return new WorkoutExerciseResponse(
                workoutExercise.getId(),
                workoutExercise.getExerciseNumber(),
                workoutExercise
                        .getExerciseDefinition()
                        .getName(),
                setResponses
        );
    }

    public SetResponse toSetResponse(
            ExerciseSet exerciseSet
    ) {
        return new SetResponse(
                exerciseSet.getId(),
                exerciseSet.getSetNumber(),
                exerciseSet.getWeight(),
                exerciseSet.getReps(),
                exerciseSet.getRir()
        );
    }

    private List<WorkoutExerciseResponse> toExerciseResponses(
            Workout workout
    ) {
        return workout.getWorkoutExercises()
                .stream()
                .sorted(
                        Comparator.comparing(
                                WorkoutExercise::getExerciseNumber
                        )
                )
                .map(this::toExerciseResponse)
                .toList();
    }
}