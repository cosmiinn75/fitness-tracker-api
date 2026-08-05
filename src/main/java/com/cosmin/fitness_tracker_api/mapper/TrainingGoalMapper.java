package com.cosmin.fitness_tracker_api.mapper;

import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.model.TrainingGoal;
import org.springframework.stereotype.Component;

@Component
public class TrainingGoalMapper {

    public TrainingGoalResponse toResponse(TrainingGoal trainingGoal){
        return new TrainingGoalResponse(
                trainingGoal.getId(),
                trainingGoal.getExerciseDefinition().getName(),
                trainingGoal.getTargetWeight(),
                trainingGoal.getTargetReps(),
                trainingGoal.getTargetDate(),
                trainingGoal.getStatus()
        );
    }

}
