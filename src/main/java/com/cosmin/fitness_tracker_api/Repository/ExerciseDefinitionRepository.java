package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseDefinitionRepository extends JpaRepository<ExerciseDefinition, Long> {
    boolean existsByName(String name);

    Optional<ExerciseDefinition> findByName(String name);

    boolean existsByNameIgnoreCase(String name);


}
