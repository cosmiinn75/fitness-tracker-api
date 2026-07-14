package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {
    List<ExerciseSet> findByWorkoutExerciseOrderBySetNumberAsc(WorkoutExercise workoutExercise);

    void deleteByWorkoutExercise(WorkoutExercise workoutExercise);

    List<ExerciseSet> findByWorkoutExercise_ExerciseDefinition_Id(Long workoutExerciseExerciseDefinitionId);

    List<ExerciseSet> findByWorkoutExerciseExerciseDefinitionIdAndWorkoutExerciseWorkoutUserUsername(Long id, String username);
}
