package com.cosmin.fitness_tracker_api.mapper;

import com.cosmin.fitness_tracker_api.DTO.SetRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutExerciseRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateExerciseResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateSetResponse;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplate;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplateExercise;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplateSet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class WorkoutTemplateMapper {

    public WorkoutTemplateResponse toResponse(
            WorkoutTemplate workoutTemplate
    ) {
        List<WorkoutTemplateExerciseResponse> exerciseResponses =
                workoutTemplate.getTemplateExercises()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        WorkoutTemplateExercise::getExerciseNumber
                                )
                        )
                        .map(this::toExerciseResponse)
                        .toList();

        return new WorkoutTemplateResponse(
                workoutTemplate.getId(),
                workoutTemplate.getTemplateName(),
                workoutTemplate.getCreatedAt(),
                exerciseResponses
        );
    }

    private WorkoutTemplateExerciseResponse toExerciseResponse(
            WorkoutTemplateExercise exercise
    ) {
        List<WorkoutTemplateSetResponse> setResponses =
                exercise.getTemplateSets()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        WorkoutTemplateSet::getSetNumber
                                )
                        )
                        .map(this::toSetResponse)
                        .toList();

        return new WorkoutTemplateExerciseResponse(
                exercise.getId(),
                exercise.getExerciseDefinition().getId(),
                exercise.getExerciseNumber(),
                exercise.getExerciseDefinition().getMuscleGroup(),
                exercise.getExerciseDefinition().getName(),
                setResponses
        );
    }

    private WorkoutTemplateSetResponse toSetResponse(
            WorkoutTemplateSet exerciseSet
    ) {
        return new WorkoutTemplateSetResponse(
                exerciseSet.getId(),
                exerciseSet.getSetNumber(),
                exerciseSet.getTargetWeight(),
                exerciseSet.getTargetReps(),
                exerciseSet.getTargetRir()
        );
    }

    public WorkoutRequest toWorkoutRequest(
            WorkoutTemplate workoutTemplate,
            LocalDate workoutDate
    ) {
        List<WorkoutExerciseRequest> exerciseRequests =
                workoutTemplate.getTemplateExercises()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        WorkoutTemplateExercise::getExerciseNumber
                                )
                        )
                        .map(exercise -> {
                            List<SetRequest> setRequests =
                                    exercise.getTemplateSets()
                                            .stream()
                                            .sorted(
                                                    Comparator.comparing(
                                                            WorkoutTemplateSet::getSetNumber
                                                    )
                                            )
                                            .map(set -> new SetRequest(
                                                    set.getTargetWeight(),
                                                    set.getTargetReps(),
                                                    set.getTargetRir()
                                            ))
                                            .toList();

                            return new WorkoutExerciseRequest(
                                    exercise.getExerciseDefinition().getId(),
                                    setRequests
                            );
                        })
                        .toList();

        return new WorkoutRequest(
                workoutTemplate.getTemplateName(),
                workoutDate,
                exerciseRequests
        );
    }
}