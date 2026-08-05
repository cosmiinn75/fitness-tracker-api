package com.cosmin.fitness_tracker_api.mapper;

import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import org.springframework.stereotype.Component;

@Component
public class ExerciseDefinitionMapper {

    public ExerciseDefinitionResponse toResponse(ExerciseDefinition exerciseDefinition){
        return new ExerciseDefinitionResponse(
                exerciseDefinition.getId(),
                exerciseDefinition.getName(),
                exerciseDefinition.getMuscleGroup(),
                exerciseDefinition.getExerciseType(),
                exerciseDefinition.isArchived()
        );
    }
}
