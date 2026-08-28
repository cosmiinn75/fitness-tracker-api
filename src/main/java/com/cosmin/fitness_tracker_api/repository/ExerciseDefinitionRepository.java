package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseDefinitionRepository extends JpaRepository<ExerciseDefinition, Long> {


    @Query(
            """
    SELECT e
    FROM ExerciseDefinition e
    LEFT JOIN e.owner owner
    WHERE e.archived = false\s
    AND (
    e.exerciseType = :exerciseType
    OR e.owner.username = :username
    )
    ORDER BY e.name
"""
    )
    List<ExerciseDefinition> findAllAccessible(
            String username,
            ExerciseType exerciseType
    );


    @Query(
            """
        SELECT e\s
        FROM ExerciseDefinition e\s
        LEFT JOIN e.owner owner
        WHERE e.archived = false\s
        AND e.id = :id\s
        AND (
        e.exerciseType = :exerciseType
        OR e.owner.username = :username
        )
"""
    )

    Optional<ExerciseDefinition> findByIdAccessible(Long id , String username , ExerciseType exerciseType);

    boolean existsByOwnerUsernameAndNormalizedName(String ownerUsername, String normalizedName);

    boolean existsByExerciseTypeAndNormalizedName(ExerciseType exerciseType, String normalizedName);


    Optional<ExerciseDefinition>
    findByIdAndOwnerUsernameAndExerciseTypeAndArchivedFalse(
            Long id,
            String username,
            ExerciseType exerciseType
    );

    boolean existsByExerciseTypeAndNormalizedNameAndMuscleGroup(ExerciseType exerciseType, String normalizedName, @NotNull MuscleGroup muscleGroup);


    boolean existsByOwnerUsernameAndNormalizedNameAndMuscleGroupAndIdNot(String username, String normalizedName, @NotNull MuscleGroup muscleGroup, Long id);

    boolean existsByOwnerUsernameAndNormalizedNameAndMuscleGroup(String username, String normalizedName, @NotNull MuscleGroup muscleGroup);
}
