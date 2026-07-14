package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.PersonalRecordResponse;
import com.cosmin.fitness_tracker_api.DTO.VolumeProgressResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutVolumeResponse;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.PersonalRecordNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.WorkoutNotFoundException;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.Workout;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutRepository;
import com.cosmin.fitness_tracker_api.Service.ProgressService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceTest {


    @Mock
    private  WorkoutRepository workoutRepository;
    @Mock
    private  ExerciseSetRepository exerciseSetRepository;
    @Mock
    private  ExerciseDefinitionRepository exerciseDefinitionRepository;

    @InjectMocks
    private ProgressService progressService;

    @Test
    void getWorkoutVolumeById_ShouldReturnTotalVolume() {
        mockAuthenticatedUser();

        Long workoutId = 1L;

        Workout workout = new Workout();
        workout.setId(workoutId);

        WorkoutExercise benchPress = getWorkoutExercise(workout);

        WorkoutExercise lateralRaise = new WorkoutExercise();
        lateralRaise.setId(2L);
        lateralRaise.setWorkout(workout);

        ExerciseSet thirdSet = new ExerciseSet();
        thirdSet.setId(3L);
        thirdSet.setWorkoutExercise(lateralRaise);
        thirdSet.setSetNumber(1);
        thirdSet.setWeight(10.0);
        thirdSet.setReps(15);
        thirdSet.setRir(1);

        lateralRaise.setExerciseSets(List.of(thirdSet));

        workout.setWorkoutExercises(List.of(benchPress, lateralRaise));

        when(workoutRepository.findByIdAndUserUsername(workoutId, "cosmin"))
                .thenReturn(Optional.of(workout));

        WorkoutVolumeResponse response =
                progressService.getWorkoutVolumeById(workoutId);


        assertEquals(1450.0, response.totalVolume(), 0.001);

        verify(workoutRepository)
                .findByIdAndUserUsername(workoutId, "cosmin");
    }

    private static @NonNull WorkoutExercise getWorkoutExercise(Workout workout) {
        WorkoutExercise benchPress = new WorkoutExercise();
        benchPress.setId(1L);
        benchPress.setWorkout(workout);

        ExerciseSet firstSet = new ExerciseSet();
        firstSet.setId(1L);
        firstSet.setWorkoutExercise(benchPress);
        firstSet.setSetNumber(1);
        firstSet.setWeight(100.0);
        firstSet.setReps(5);
        firstSet.setRir(1);

        ExerciseSet secondSet = new ExerciseSet();
        secondSet.setId(2L);
        secondSet.setWorkoutExercise(benchPress);
        secondSet.setSetNumber(2);
        secondSet.setWeight(80.0);
        secondSet.setReps(10);
        secondSet.setRir(2);

        benchPress.setExerciseSets(List.of(firstSet, secondSet));
        return benchPress;
    }

    private void mockAuthenticatedUser() {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "cosmin",
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }


    @Test
    void getWorkoutVolumeById_ShouldThrowException_WhenWorkoutDoesNotExist() {

        mockAuthenticatedUser();
        Long workoutId = 1L;

        when(workoutRepository.findByIdAndUserUsername(workoutId, "cosmin"))
                .thenReturn(Optional.empty());


        assertThrows(WorkoutNotFoundException.class,
                () -> progressService.getWorkoutVolumeById(workoutId));

        verify(workoutRepository).findByIdAndUserUsername(workoutId, "cosmin");
    }

    @Test
    void getWeeklyVolume_ShouldReturnTotalVolume() {
        mockAuthenticatedUser();

        LocalDate today = LocalDate.now();
        LocalDate aWeekAgo = today.minusDays(6);

        Workout firstWorkout = createWorkoutWithSet(1L,100.00,5);
        Workout secondWorkout = createWorkoutWithSet(2L,80.00,10);

        when(workoutRepository.findByUserUsernameAndDateBetween("cosmin", aWeekAgo, today))
        .thenReturn(List.of(firstWorkout, secondWorkout));


        VolumeProgressResponse response = progressService.getWeeklyVolume();

        assertEquals(1300.0, response.volume(), 0.001);
        assertEquals(aWeekAgo, response.startDate());
        assertEquals(today, response.endDate());

        // 100 * 5 + 80 * 10 = 1300
        verify(workoutRepository).findByUserUsernameAndDateBetween(
                "cosmin",
                aWeekAgo,
                today
        );

    }

    @Test
    void getMonthyVolume_ShouldReturnTotalVolume() {
        mockAuthenticatedUser();

        LocalDate today = LocalDate.now();
        LocalDate aMonthAgo = today.withDayOfMonth(1);

        Workout firstWorkout = createWorkoutWithSet(1L,100.00,5);
        Workout secondWorkout = createWorkoutWithSet(2L,80.00,5);

        when(workoutRepository.findByUserUsernameAndDateBetween("cosmin", aMonthAgo, today))
                .thenReturn(List.of(firstWorkout, secondWorkout));


        VolumeProgressResponse response = progressService.getMonthlyVolume();

        assertEquals(900.00, response.volume(), 0.001);
        assertEquals(aMonthAgo, response.startDate());
        assertEquals(today, response.endDate());

        // 100 * 5 + 80 * 5 = 900
        verify(workoutRepository).findByUserUsernameAndDateBetween(
                "cosmin",
                aMonthAgo,
                today
        );

    }


    @Test
    void getPersonalRecordById_ShouldChooseHighestWeight() {
        mockAuthenticatedUser();

        Long exerciseDefinitionId = 1L;

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(exerciseDefinitionId);
        exerciseDefinition.setName("Bench Press");

        ExerciseSet lighterSet = createExerciseSet(
                1L,
                90.0,
                10,
                LocalDate.of(2026, 7, 10)
        );

        ExerciseSet heavierSet = createExerciseSet(
                2L,
                100.0,
                5,
                LocalDate.of(2026, 7, 12)
        );

        when(exerciseDefinitionRepository.findById(exerciseDefinitionId))
                .thenReturn(Optional.of(exerciseDefinition));

        when(exerciseSetRepository
                .findByWorkoutExerciseExerciseDefinitionIdAndWorkoutExerciseWorkoutUserUsername(
                        exerciseDefinitionId,
                        "cosmin"
                ))
                .thenReturn(List.of(lighterSet, heavierSet));

        PersonalRecordResponse response =
                progressService.getPersonalRecordById(exerciseDefinitionId);

        assertEquals(exerciseDefinitionId, response.exerciseDefinitionId());
        assertEquals("Bench Press", response.exerciseName());
        assertEquals(100.0, response.weight(), 0.001);
        assertEquals(5, response.reps());
        assertEquals(LocalDate.of(2026, 7, 12), response.date());
    }

    private Workout createWorkoutWithSet(
            Long workoutId,
            double weight,
            int reps
    ) {
        Workout workout = new Workout();
        workout.setId(workoutId);

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(workoutId);
        workoutExercise.setWorkout(workout);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(workoutId);
        exerciseSet.setWorkoutExercise(workoutExercise);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(weight);
        exerciseSet.setReps(reps);

        workoutExercise.setExerciseSets(List.of(exerciseSet));
        workout.setWorkoutExercises(List.of(workoutExercise));

        return workout;
    }

    @Test
    void getPersonalRecordById_ShouldChooseHighestReps_WhenWeightsAreEqual() {
        mockAuthenticatedUser();

        Long exerciseDefinitionId = 1L;

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(exerciseDefinitionId);
        exerciseDefinition.setName("Bench Press");

        ExerciseSet firstSet = createExerciseSet(
                1L,
                100.0,
                5,
                LocalDate.of(2026, 7, 10)
        );

        ExerciseSet secondSet = createExerciseSet(
                2L,
                100.0,
                8,
                LocalDate.of(2026, 7, 12)
        );

        when(exerciseDefinitionRepository.findById(exerciseDefinitionId))
                .thenReturn(Optional.of(exerciseDefinition));

        when(exerciseSetRepository
                .findByWorkoutExerciseExerciseDefinitionIdAndWorkoutExerciseWorkoutUserUsername(
                        exerciseDefinitionId,
                        "cosmin"
                ))
                .thenReturn(List.of(firstSet, secondSet));

        PersonalRecordResponse response =
                progressService.getPersonalRecordById(exerciseDefinitionId);

        assertEquals(100.0, response.weight(), 0.001);
        assertEquals(8, response.reps());
        assertEquals(LocalDate.of(2026, 7, 12), response.date());
    }


    @Test
    void getPersonalRecordById_ShouldThrowException_WhenExerciseDoesNotExist() {
        mockAuthenticatedUser();

        Long exerciseDefinitionId = 1L;

        when(exerciseDefinitionRepository.findById(exerciseDefinitionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExerciseDefinitionNotFoundException.class,
                () -> progressService.getPersonalRecordById(exerciseDefinitionId)
        );

        verify(exerciseDefinitionRepository).findById(exerciseDefinitionId);

        verifyNoInteractions(exerciseSetRepository);
    }

    @Test
    void getPersonalRecordById_ShouldThrowException_WhenNoSetsExist() {
        mockAuthenticatedUser();

        Long exerciseDefinitionId = 1L;

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(exerciseDefinitionId);
        exerciseDefinition.setName("Bench Press");

        when(exerciseDefinitionRepository.findById(exerciseDefinitionId))
                .thenReturn(Optional.of(exerciseDefinition));

        when(exerciseSetRepository
                .findByWorkoutExerciseExerciseDefinitionIdAndWorkoutExerciseWorkoutUserUsername(
                        exerciseDefinitionId,
                        "cosmin"
                ))
                .thenReturn(List.of());

        RuntimeException exception = assertThrows(
                PersonalRecordNotFoundException.class,
                () -> progressService.getPersonalRecordById(exerciseDefinitionId)
        );

        assertEquals(
                "No sets found for this exercise",
                exception.getMessage()
        );
    }


    private ExerciseSet createExerciseSet(
            Long setId,
            double weight,
            int reps,
            LocalDate workoutDate
    ) {
        Workout workout = new Workout();
        workout.setId(setId);
        workout.setDate(workoutDate);

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(setId);
        workoutExercise.setWorkout(workout);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(setId);
        exerciseSet.setWeight(weight);
        exerciseSet.setReps(reps);
        exerciseSet.setWorkoutExercise(workoutExercise);

        return exerciseSet;
    }

}
