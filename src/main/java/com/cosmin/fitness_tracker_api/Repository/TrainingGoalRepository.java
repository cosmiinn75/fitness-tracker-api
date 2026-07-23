package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.TrainingGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingGoalRepository extends JpaRepository<TrainingGoal,Long> {
    boolean existsByUserUsernameAndExerciseDefinitionIdAndStatus(String username, Long exerciseDefinitionId, Status status);


    Optional<TrainingGoal> findByUserUsernameAndExerciseDefinitionIdAndStatus(String userUsername, Long exerciseDefinitionId, Status status);

    Optional<TrainingGoal> findByUserUsernameAndId(String userUsername, Long id);

    Page<TrainingGoal> findByUserUsernameOrderByIdAsc(String userUsername, Pageable pageable);

    List<TrainingGoal> findByUserUsernameOrderByIdAsc(String userUsername);
}
