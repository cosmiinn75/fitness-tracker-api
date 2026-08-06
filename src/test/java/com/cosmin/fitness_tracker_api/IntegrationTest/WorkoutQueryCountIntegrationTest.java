package com.cosmin.fitness_tracker_api.IntegrationTest;

import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.model.*;
import com.cosmin.fitness_tracker_api.repository.*;
import com.cosmin.fitness_tracker_api.service.WorkoutService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkoutQueryCountIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private  WorkoutRepository workoutRepository;

    @Autowired
    private  WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private ExerciseSetRepository exerciseSetRepository;

    @Autowired
    private  ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private WorkoutService workoutService;

    @Test
    @WithMockUser(username = "cosmin")
    void getWorkoutById_ShouldNotProduceNPlusOneQueries() {

        User user = createUser();

        ExerciseDefinition exerciseDefinition1 = createExerciseDefinition("Bench", "bench");

        ExerciseDefinition exerciseDefinition2 = createExerciseDefinition("Deadlift", "deadlift");

        ExerciseDefinition exerciseDefinition3 = createExerciseDefinition("Squat","squat");

        Workout workout= createWorkout("Workout", user);

        WorkoutExercise exercise1 = createWorkoutExercise(workout, 1, exerciseDefinition1);

        WorkoutExercise exercise2 = createWorkoutExercise(workout, 2, exerciseDefinition2);

        WorkoutExercise exercise3 = createWorkoutExercise(workout, 3, exerciseDefinition3);

        createExerciseSet(exercise1, 1,100.0,5,0);

        createExerciseSet(exercise1, 2,100.0,5,0);

        createExerciseSet(exercise2, 1,100.0,5,0);

        createExerciseSet(exercise2, 2,100.0,5,0);

        createExerciseSet(exercise3, 1,100.0,5,0);

        createExerciseSet(exercise3, 2,100.0,5,0);

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory =
                entityManagerFactory.unwrap(
                        SessionFactory.class
                );

        Statistics statistics =
                sessionFactory.getStatistics();

        statistics.setStatisticsEnabled(true);
        statistics.clear();

        WorkoutResponse response =workoutService.getWorkoutById(workout.getId());

        long executedStatements = statistics.getPrepareStatementCount();

        assertEquals(workout.getId(),
                response.id()
        );

        assertEquals("Workout",
                response.workoutName()
        );

        assertEquals(3,
                response.exerciseResponses().size()
        );

        assertEquals("Bench",
                response.exerciseResponses()
                        .getFirst()
                        .exerciseName()
        );

        assertEquals("Deadlift",
                response.exerciseResponses()
                        .get(1)
                        .exerciseName()
        );

        assertEquals("Squat",
                response.exerciseResponses()
                        .get(2)
                        .exerciseName()
        );

        assertTrue(response.exerciseResponses()
                        .stream()
                        .allMatch(exercise ->
                                exercise.setResponses().size() == 2
                        )
        );

        assertEquals(2L, executedStatements);
    }

    @Test
    @WithMockUser(username = "cosmin")
    void getAllWorkoutsFiltered_ShouldUseConstantNumberOfQueries() {
        User user = createUser();

        for (int workoutIndex = 1; workoutIndex <= 4; workoutIndex++) {
            Workout workout = createWorkout("Push " + workoutIndex, user);
            workout.setDate(LocalDate.of(2025, 2, 9 + workoutIndex));
            workoutRepository.saveAndFlush(workout);

            for (int exerciseIndex = 1; exerciseIndex <= 3; exerciseIndex++) {
                ExerciseDefinition exerciseDefinition = createExerciseDefinition(
                        "Exercise " + workoutIndex + "-" + exerciseIndex,
                        "exercise-" + workoutIndex + "-" + exerciseIndex
                );

                WorkoutExercise workoutExercise = createWorkoutExercise(
                        workout,
                        exerciseIndex,
                        exerciseDefinition
                );

                createExerciseSet(workoutExercise, 1, 100.0, 5, 1);
                createExerciseSet(workoutExercise, 2, 105.0, 4, 1);
            }
        }

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory =
                entityManagerFactory.unwrap(SessionFactory.class);

        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        PagedResponse<WorkoutResponse> response =
                workoutService.getAllWorkoutsFiltered(
                        0,
                        2,
                        "Push",
                        LocalDate.of(2025, 2, 1),
                        LocalDate.of(2025, 2, 28)
                );

        long executedStatements =
                statistics.getPrepareStatementCount();

        assertEquals(2, response.content().size());
        assertEquals(4L, response.totalElements());

        assertTrue(
                response.content()
                        .stream()
                        .allMatch(workout ->
                                workout.exerciseResponses().size() == 3
                        )
        );

        assertTrue(
                response.content()
                        .stream()
                        .flatMap(workout ->
                                workout.exerciseResponses().stream()
                        )
                        .allMatch(exercise ->
                                exercise.setResponses().size() == 2
                        )
        );

        assertEquals(4L, executedStatements);
    }

    private User createUser(){
        User user = new User();

        user.setUsername("cosmin");
        user.setPassword("encoded-password");
        user.setEmail("cosmin@gmail.com");

        return userRepository.saveAndFlush(user);
    }


    private Workout createWorkout(String workoutName, User user ){

        Workout workout = new Workout();

        workout.setUser(user);
        workout.setWorkoutName(workoutName);
        workout.setDate(LocalDate.now());

        return workoutRepository.saveAndFlush(workout);

    }


    private ExerciseDefinition createExerciseDefinition(String exerciseName , String normalizedName) {
        ExerciseDefinition  exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName(exerciseName);
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setNormalizedName(normalizedName);


        return exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);
    }

    private WorkoutExercise createWorkoutExercise(Workout workout, Integer exerciseNumber  , ExerciseDefinition exerciseDefinition ){

        WorkoutExercise exercise = new WorkoutExercise();
        exercise.setExerciseDefinition(exerciseDefinition);
        exercise.setWorkout(workout);
        exercise.setExerciseNumber(exerciseNumber);

        return workoutExerciseRepository.saveAndFlush(exercise);
    }

    private ExerciseSet createExerciseSet(WorkoutExercise exercise,Integer setNumber , Double weight, Integer reps, Integer rir){

        ExerciseSet set = new ExerciseSet();
        set.setSetNumber(setNumber);
        set.setReps(reps);
        set.setRir(rir);
        set.setWeight(weight);
        set.setWorkoutExercise(exercise);

        return  exerciseSetRepository.saveAndFlush(set);

    }

}
