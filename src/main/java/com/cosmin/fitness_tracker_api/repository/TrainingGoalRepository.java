package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.model.TrainingGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TrainingGoalRepository extends JpaRepository<TrainingGoal,Long> {
    boolean existsByUserUsernameAndExerciseDefinitionIdAndStatus(String username, Long exerciseDefinitionId, Status status);


    Optional<TrainingGoal> findByUserUsernameAndExerciseDefinitionIdAndStatus(String userUsername, Long exerciseDefinitionId, Status status);

    Optional<TrainingGoal> findByUserUsernameAndId(String userUsername, Long id);

    Page<TrainingGoal> findByUserUsernameOrderByIdAsc(String userUsername, Pageable pageable);



    @Query(
            value = """
        UPDATE TrainingGoal
        SET status = :expiredStatus
        WHERE targetDate < :currentDate
        AND status = :activeStatus
"""
    )
    @Modifying
    void updateTrainingGoalStatus(LocalDate currentDate,Status expiredStatus,Status activeStatus);
}
