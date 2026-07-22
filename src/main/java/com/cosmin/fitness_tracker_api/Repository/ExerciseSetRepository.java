package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.Repository.Projection.PersonalRecordProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {
    List<ExerciseSet> findByWorkoutExerciseOrderBySetNumberAsc(WorkoutExercise workoutExercise);

    void deleteByWorkoutExercise(WorkoutExercise workoutExercise);


    List<ExerciseSet> findByWorkoutExerciseExerciseDefinitionIdAndWorkoutExerciseWorkoutUserUsername(Long id, String username);

    @Query(nativeQuery = true,
   value = """
SELECT ranked.exercise_definition_id AS exerciseDefinitionId,
       ed.name AS exerciseName,
       ranked.weight AS weight,
       ranked.reps AS reps,
       ranked.rir AS rir,
       ranked.date  AS workoutDate
FROM
(SELECT
    we.exercise_definition_id,
   es.weight,
    es.reps,
    es.rir,
    wo.date,
    wo.id AS workout_id,
    es.set_number,
    u.username,
    ROW_NUMBER() OVER(PARTITION BY we.exercise_definition_id ORDER BY
        es.weight desc,
        es.reps desc,
        es.rir desc,
        wo.date desc,
        wo.id desc,
        es.set_number desc)  AS RowNumber
FROM exercise_sets es
JOIN workout_exercises we
ON es.workout_exercise_id = we.id
JOIN workouts wo
ON we.workout_id = wo.id
JOIN users u
ON wo.user_id = u.id WHERE u.username = :username ) AS ranked

JOIN exercise_definitions ed
ON exercise_definition_id = ed.id
WHERE ranked.RowNumber = 1
ORDER BY exercise_definition_id

""",
    countQuery = """
        SELECT COUNT(DISTINCT we.exercise_definition_id)
        FROM exercise_sets es
        JOIN workout_exercises we
        ON es.workout_exercise_id = we.id
        JOIN workouts wo
        ON we.workout_id = wo.id
        JOIN users u
        ON wo.user_id = u.id
        WHERE u.username = :username
""")
    Page<PersonalRecordProjection> findBestExerciseSets(
            @Param("username") String username,
            Pageable pageable
    );
}
