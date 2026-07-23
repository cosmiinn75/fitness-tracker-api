package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.TrainingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingGoalRepository extends JpaRepository<TrainingGoal,Long> {
    boolean existsByUserUsernameAndExerciseDefinitionIdAndStatus(String username, Long exerciseDefinitionId, Status status);

    List<TrainingGoal> findByUserUsernameAndExerciseDefinition(String userUsername, ExerciseDefinition exerciseDefinition);

    Optional<TrainingGoal> findByUserUsernameAndExerciseDefinitionIdAndStatus(String userUsername, Long exerciseDefinitionId, Status status);
}
