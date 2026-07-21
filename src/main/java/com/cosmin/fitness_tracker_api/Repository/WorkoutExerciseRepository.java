package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.Workout;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    List<WorkoutExercise> findByWorkoutOrderByExerciseNumberAsc(Workout workout);

    void deleteByWorkout(Workout workout);

    @Query(
            """
            SELECT we
            FROM WorkoutExercise we
            WHERE (we.exerciseDefinition.id = :exerciseDefinitionId)
            AND (we.workout.user.username = :username)
            AND (:startDate IS NULL OR we.workout.date >= :startDate)
            AND (:endDate IS NULL OR we.workout.date <= :endDate)
            ORDER BY we.workout.date DESC,we.id DESC
            """
    )
    Page<WorkoutExercise> findHistoryByExerciseDefinitionIdAndWorkoutDate(Long exerciseDefinitionId, String username , LocalDate startDate, LocalDate endDate, Pageable pageable);
    
}
