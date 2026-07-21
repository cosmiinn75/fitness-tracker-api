package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.InvalidDateRangeException;
import com.cosmin.fitness_tracker_api.Exception.PersonalRecordNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.WorkoutNotFoundException;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutExerciseRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutRepository;
import com.cosmin.fitness_tracker_api.Service.ProgressService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceTest {


    @Mock
    private  WorkoutRepository workoutRepository;
    @Mock
    private  ExerciseSetRepository exerciseSetRepository;
    @Mock
    private  ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

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
                progressService.getPersonalRecordByExerciseDefinitionId(exerciseDefinitionId);

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
                progressService.getPersonalRecordByExerciseDefinitionId(exerciseDefinitionId);

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
                () -> progressService.getPersonalRecordByExerciseDefinitionId(exerciseDefinitionId)
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
                () -> progressService.getPersonalRecordByExerciseDefinitionId(exerciseDefinitionId)
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


    @Test
    void getWorkoutHistory_ShouldReturnWorkoutHistory(){
        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Long exerciseDefinitionId = 1L;
        LocalDate startDate = LocalDate.of(2026,7,15);
        LocalDate endDate = LocalDate.of(2026,7,16);
        int page = 0;
        int size = 10;

        Pageable pageable = PageRequest.of(page,size);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        Workout workout = new Workout();
        workout.setId(1L);
        workout.setUser(user);
        workout.setWorkoutName("Push");
        workout.setDate(LocalDate.of(2026, 7, 15));


        when(exerciseDefinitionRepository.findById(exerciseDefinitionId)).thenReturn(Optional.of(exerciseDefinition));

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(1L);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(100.0);
        exerciseSet.setReps(5);
        exerciseSet.setRir(1);

        WorkoutExercise workoutExercise = new WorkoutExercise();

        exerciseSet.setWorkoutExercise(workoutExercise);

        workoutExercise.setId(1L);
        workoutExercise.setExerciseDefinition(exerciseDefinition);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExerciseSets(List.of(exerciseSet));
        workoutExercise.setExerciseNumber(1);


        Page<WorkoutExercise> workoutExercises = new PageImpl<>(
                List.of(workoutExercise),
                pageable,
                1
        );

        when(workoutExerciseRepository.findHistoryByExerciseDefinitionIdAndWorkoutDate(
                exerciseDefinitionId,
                "cosmin",
                startDate,
                endDate,
                pageable
        )).thenReturn(workoutExercises);

        PagedResponse<WorkoutExerciseHistoryResponse> response = progressService.getWorkoutHistory(
                exerciseDefinitionId,
                startDate,
                endDate,
                page,
                size
        );

        assertNotNull(response);

        assertEquals(1, response.content().size());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());

        WorkoutExerciseHistoryResponse historyResponse =
                response.content().getFirst();

        assertEquals(1L, historyResponse.workoutId());
        assertEquals(1L, historyResponse.workoutExerciseId());
        assertEquals(1, historyResponse.exerciseNumber());
        assertEquals("Bench Press", historyResponse.exerciseName());
        assertEquals(
                LocalDate.of(2026, 7, 15),
                historyResponse.workoutDate()
        );
        assertEquals(116.67,historyResponse.estimatedOneRepMax(),0.1);

        assertEquals(1, historyResponse.setResponses().size());

        SetResponse setResponse =
                historyResponse.setResponses().getFirst();

        assertEquals(1L, setResponse.id());
        assertEquals(1, setResponse.setNumber());
        assertEquals(100.0, setResponse.weight());
        assertEquals(5, setResponse.reps());
        assertEquals(1, setResponse.rir());

        verify(exerciseDefinitionRepository)
                .findById(exerciseDefinitionId);

        verify(workoutExerciseRepository)
                .findHistoryByExerciseDefinitionIdAndWorkoutDate(
                        exerciseDefinitionId,
                        "cosmin",
                        startDate,
                        endDate,
                        pageable
                );

    }

    @Test
    void getWorkoutHistory_ShouldThrowException_WhenDateRangeIsInvalid() {
        mockAuthenticatedUser();

        Long exerciseDefinitionId = 1L;

        LocalDate startDate = LocalDate.of(2026, 7, 20);
        LocalDate endDate = LocalDate.of(2026, 7, 10);

        assertThrows(
                InvalidDateRangeException.class,
                () -> progressService.getWorkoutHistory(
                        exerciseDefinitionId,
                        startDate,
                        endDate,
                        0,
                        10
                )
        );
    }

    @Test
    void getWorkoutHistory_ShouldThrowException_WhenExerciseDefinitionDoesNotExist() {
        mockAuthenticatedUser();

        Long exerciseDefinitionId = 1L;

        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 7, 20);

        when(exerciseDefinitionRepository.findById(exerciseDefinitionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ExerciseDefinitionNotFoundException.class,
                () -> progressService.getWorkoutHistory(
                        exerciseDefinitionId,
                        startDate,
                        endDate,
                        0,
                        10
                )
        );

        verify(exerciseDefinitionRepository)
                .findById(exerciseDefinitionId);
    }

    @Test
    void summary_ShouldReturnSummaryResponse(){
        mockAuthenticatedUser();
        User user = new User();
        user.setUsername("cosmin");

        LocalDate today = LocalDate.now();

        LocalDate workoutDate = today.minusDays(1);

        long totalWorkouts = 1;
        when(workoutRepository.countByUserUsername("cosmin")).thenReturn(totalWorkouts);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        LocalDate aMonthAgo = today.minusDays(29);

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setDate(workoutDate);
        workout.setWorkoutName("Push");

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setExerciseDefinition(exerciseDefinition);
        workoutExercise.setWorkout(workout);
        workoutExercise.setExerciseNumber(1);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setRir(1);
        exerciseSet.setReps(5);
        exerciseSet.setWeight(100.0);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWorkoutExercise(workoutExercise);

        workoutExercise.setExerciseSets(List.of(exerciseSet));

        workout.setWorkoutExercises(List.of(workoutExercise));

        List<Workout> workouts = List.of(workout);

        when(workoutRepository.findByUserUsernameAndDateBetween("cosmin", aMonthAgo, today))
                .thenReturn(workouts);
        when(workoutRepository.findFirstByUserUsernameOrderByDateDesc("cosmin"))
                .thenReturn(Optional.of(workout));

        SummaryResponse response = progressService.getSummary();

        assertNotNull(response);

        assertEquals(1, response.totalWorkouts());
        assertEquals(1, response.trainingDaysLast7Days());
        assertEquals(1, response.trainingDaysLast30Days());
        assertEquals(1, response.totalSetsLast7Days());
        assertEquals(workoutDate, response.lastWorkoutDate());
        assertEquals("Bench Press", response.mostTrainedExerciseLast30Days());

        verify(workoutRepository)
                .countByUserUsername("cosmin");

        verify(workoutRepository)
                .findByUserUsernameAndDateBetween(
                        "cosmin",
                        aMonthAgo,
                        today
                );

        verify(workoutRepository)
                .findFirstByUserUsernameOrderByDateDesc("cosmin");

    }


    private void mockAuthenticatedUser() {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "cosmin",
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
