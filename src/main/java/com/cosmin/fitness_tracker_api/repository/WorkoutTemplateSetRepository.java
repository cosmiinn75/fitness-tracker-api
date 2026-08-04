package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.model.WorkoutTemplateExercise;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplateSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutTemplateSetRepository extends JpaRepository<WorkoutTemplateSet,Long> {

    List<WorkoutTemplateSet> findByWorkoutTemplateExerciseOrderBySetNumberAsc(WorkoutTemplateExercise templateExercise);
}
