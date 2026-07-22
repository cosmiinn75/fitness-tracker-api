package com.cosmin.fitness_tracker_api.IntegrationTest;

import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Model.Workout;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.Repository.RefreshTokenRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutExerciseRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutRepository;
import com.cosmin.fitness_tracker_api.Repository.Projection.PersonalRecordProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class PersonalRecordRepositoryIntegrationTest {

    @Autowired
    private ExerciseSetRepository exerciseSetRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        exerciseSetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutRepository.deleteAll();
        exerciseDefinitionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findBestExerciseSets_ShouldApplyPriorityAndExcludeOtherUsers() {
        User cosmin = createUser("cosmin", "cosmin@example.com");
        User anotherUser = createUser("another_user", "another@example.com");

        ExerciseDefinition benchPress = createExerciseDefinition(
                "Bench Press",
                MuscleGroup.CHEST
        );
        ExerciseDefinition squat = createExerciseDefinition(
                "Squat",
                MuscleGroup.LEGS
        );

        Workout olderWorkout = createWorkout(cosmin, LocalDate.of(2026, 7, 10));
        Workout middleWorkout = createWorkout(cosmin, LocalDate.of(2026, 7, 11));
        Workout newerWorkout = createWorkout(cosmin, LocalDate.of(2026, 7, 12));
        Workout otherUsersWorkout = createWorkout(anotherUser, LocalDate.of(2026, 7, 20));

        WorkoutExercise olderBench = createWorkoutExercise(olderWorkout, benchPress, 1);
        createSet(olderBench, 1, 90.0, 20, 3);
        createSet(olderBench, 2, 100.0, 5, 3);
        createSet(olderBench, 3, 100.0, 6, 0);

        WorkoutExercise middleBench = createWorkoutExercise(middleWorkout, benchPress, 1);
        createSet(middleBench, 1, 100.0, 6, 2);

        WorkoutExercise newerBench = createWorkoutExercise(newerWorkout, benchPress, 1);
        createSet(newerBench, 1, 100.0, 6, 2);

        WorkoutExercise squatExercise = createWorkoutExercise(newerWorkout, squat, 2);
        createSet(squatExercise, 1, 140.0, 5, 1);

        WorkoutExercise otherUsersBench = createWorkoutExercise(
                otherUsersWorkout,
                benchPress,
                1
        );
        createSet(otherUsersBench, 1, 200.0, 10, 3);

        Page<PersonalRecordProjection> result = exerciseSetRepository
                .findBestExerciseSets("cosmin", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());

        PersonalRecordProjection benchRecord = findByName(result, "Bench Press");
        assertEquals(benchPress.getId(), benchRecord.getExerciseDefinitionId());
        assertEquals(100.0, benchRecord.getWeight());
        assertEquals(6, benchRecord.getReps());
        assertEquals(2, benchRecord.getRir());
        assertEquals(LocalDate.of(2026, 7, 12), benchRecord.getWorkoutDate());

        PersonalRecordProjection squatRecord = findByName(result, "Squat");
        assertEquals(squat.getId(), squatRecord.getExerciseDefinitionId());
        assertEquals(140.0, squatRecord.getWeight());
        assertEquals(5, squatRecord.getReps());

        assertTrue(result.getContent().stream()
                .noneMatch(record -> record.getWeight().equals(200.0)));
    }

    @Test
    void findBestExerciseSets_ShouldReturnCorrectPaginationMetadata() {
        User cosmin = createUser("cosmin", "cosmin@example.com");
        ExerciseDefinition benchPress = createExerciseDefinition(
                "Bench Press",
                MuscleGroup.CHEST
        );
        ExerciseDefinition squat = createExerciseDefinition(
                "Squat",
                MuscleGroup.LEGS
        );

        Workout workout = createWorkout(cosmin, LocalDate.of(2026, 7, 22));
        createSet(createWorkoutExercise(workout, benchPress, 1), 1, 105.0, 3, 0);
        createSet(createWorkoutExercise(workout, squat, 2), 1, 145.0, 4, 0);

        Page<PersonalRecordProjection> firstPage = exerciseSetRepository
                .findBestExerciseSets("cosmin", PageRequest.of(0, 1));
        Page<PersonalRecordProjection> secondPage = exerciseSetRepository
                .findBestExerciseSets("cosmin", PageRequest.of(1, 1));

        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(1, firstPage.getContent().size());
        assertTrue(firstPage.isFirst());
        assertFalse(firstPage.isLast());

        assertEquals(2, secondPage.getTotalElements());
        assertEquals(2, secondPage.getTotalPages());
        assertEquals(1, secondPage.getContent().size());
        assertFalse(secondPage.isFirst());
        assertTrue(secondPage.isLast());

        assertNotEquals(
                firstPage.getContent().getFirst().getExerciseDefinitionId(),
                secondPage.getContent().getFirst().getExerciseDefinitionId()
        );
    }

    @Test
    void findBestExerciseSets_ShouldReturnEmptyPage_WhenUserHasNoSets() {
        createUser("cosmin", "cosmin@example.com");

        Page<PersonalRecordProjection> result = exerciseSetRepository
                .findBestExerciseSets("cosmin", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    private PersonalRecordProjection findByName(
            Page<PersonalRecordProjection> page,
            String exerciseName
    ) {
        return page.getContent().stream()
                .filter(record -> exerciseName.equals(record.getExerciseName()))
                .findFirst()
                .orElseThrow();
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("test-password");
        return userRepository.saveAndFlush(user);
    }

    private ExerciseDefinition createExerciseDefinition(
            String name,
            MuscleGroup muscleGroup
    ) {
        ExerciseDefinition definition = new ExerciseDefinition();
        definition.setName(name);
        definition.setMuscleGroup(muscleGroup);
        return exerciseDefinitionRepository.saveAndFlush(definition);
    }

    private Workout createWorkout(User user, LocalDate date) {
        Workout workout = new Workout();
        workout.setUser(user);
        workout.setWorkoutName("Test workout");
        workout.setDate(date);
        return workoutRepository.saveAndFlush(workout);
    }

    private WorkoutExercise createWorkoutExercise(
            Workout workout,
            ExerciseDefinition definition,
            int exerciseNumber
    ) {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkout(workout);
        workoutExercise.setExerciseDefinition(definition);
        workoutExercise.setExerciseNumber(exerciseNumber);
        return workoutExerciseRepository.saveAndFlush(workoutExercise);
    }

    private ExerciseSet createSet(
            WorkoutExercise workoutExercise,
            int setNumber,
            double weight,
            int reps,
            int rir
    ) {
        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setWorkoutExercise(workoutExercise);
        exerciseSet.setSetNumber(setNumber);
        exerciseSet.setWeight(weight);
        exerciseSet.setReps(reps);
        exerciseSet.setRir(rir);
        return exerciseSetRepository.saveAndFlush(exerciseSet);
    }
}