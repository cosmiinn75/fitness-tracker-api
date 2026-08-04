package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.model.WorkoutTemplate;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplateExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutTemplateExerciseRepository extends JpaRepository<WorkoutTemplateExercise,Long> {

    List<WorkoutTemplateExercise> findByWorkoutTemplateOrderByExerciseNumberAsc(WorkoutTemplate workoutTemplate);
}
