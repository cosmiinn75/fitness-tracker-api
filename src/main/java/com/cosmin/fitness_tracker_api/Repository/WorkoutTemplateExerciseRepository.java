package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.WorkoutTemplate;
import com.cosmin.fitness_tracker_api.Model.WorkoutTemplateExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutTemplateExerciseRepository extends JpaRepository<WorkoutTemplateExercise,Long> {

    List<WorkoutTemplateExercise> findByWorkoutTemplateOrderByExerciseNumberAsc(WorkoutTemplate workoutTemplate);
}
