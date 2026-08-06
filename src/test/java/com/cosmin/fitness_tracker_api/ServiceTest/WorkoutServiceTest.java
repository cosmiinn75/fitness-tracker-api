package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.exception.WorkoutNotFoundException;
import com.cosmin.fitness_tracker_api.mapper.WorkoutMapper;
import com.cosmin.fitness_tracker_api.model.*;
import com.cosmin.fitness_tracker_api.repository.*;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import com.cosmin.fitness_tracker_api.service.TrainingGoalService;
import com.cosmin.fitness_tracker_api.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTest {

    @Mock
    private ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private ExerciseSetRepository exerciseSetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingGoalService trainingGoalService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private WorkoutMapper workoutMapper = new WorkoutMapper();

    @InjectMocks
    private WorkoutService workoutService;

    @BeforeEach
    void setup() {
        when(currentUserProvider.getCurrentUsername())
                .thenReturn("cosmin");
    }

    @Test
    void createWorkout_WithValidData_ShouldCreateWorkout() {
        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition =
                new ExerciseDefinition();

        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");

        WorkoutRequest workoutRequest =
                new WorkoutRequest(
                        "push",
                        LocalDate.of(2025, 2, 10),
                        List.of(
                                new WorkoutExerciseRequest(
                                        1L,
                                        List.of(
                                                new SetRequest(
                                                        60.0,
                                                        10,
                                                        2
                                                ),
                                                new SetRequest(
                                                        70.0,
                                                        8,
                                                        1
                                                )
                                        )
                                )
                        )
                );

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(
                exerciseDefinitionRepository.findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                )
        ).thenReturn(Optional.of(exerciseDefinition));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> {
                    Workout savedWorkout =
                            invocation.getArgument(0);

                    savedWorkout.setId(1L);

                    return savedWorkout;
                });

        when(
                workoutExerciseRepository.save(
                        any(WorkoutExercise.class)
                )
        ).thenAnswer(invocation -> {
            WorkoutExercise savedExercise =
                    invocation.getArgument(0);

            savedExercise.setId(1L);

            return savedExercise;
        });

        when(exerciseSetRepository.save(any(ExerciseSet.class)))
                .thenAnswer(invocation -> {
                    ExerciseSet savedSet =
                            invocation.getArgument(0);

                    savedSet.setId(1L);

                    return savedSet;
                });

        when(
                trainingGoalService.completeGoalsFromWorkout(
                        any(Workout.class)
                )
        ).thenReturn(2);

        CreateWorkoutResponse response =
                workoutService.createWorkout(workoutRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("push", response.workoutName());

        assertEquals(
                LocalDate.of(2025, 2, 10),
                response.date()
        );

        assertEquals(
                1,
                response.exerciseResponses().size()
        );

        assertEquals(2, response.goalsCompleted());

        WorkoutExerciseResponse exerciseResponse =
                response.exerciseResponses().getFirst();

        assertEquals(1L, exerciseResponse.id());
        assertEquals(1, exerciseResponse.exerciseNumber());
        assertEquals(
                "Bench Press",
                exerciseResponse.exerciseName()
        );

        assertEquals(
                2,
                exerciseResponse.setResponses().size()
        );

        SetResponse firstSet =
                exerciseResponse.setResponses().getFirst();

        assertEquals(1L, firstSet.id());
        assertEquals(1, firstSet.setNumber());
        assertEquals(60.0, firstSet.weight(), 0.001);
        assertEquals(10, firstSet.reps());
        assertEquals(2, firstSet.rir());

        verify(userRepository)
                .findByUsername("cosmin");

        verify(exerciseDefinitionRepository)
                .findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                );

        verify(workoutRepository)
                .save(any(Workout.class));

        verify(workoutExerciseRepository)
                .save(any(WorkoutExercise.class));

        verify(exerciseSetRepository, times(2))
                .save(any(ExerciseSet.class));

        verify(trainingGoalService)
                .completeGoalsFromWorkout(
                        any(Workout.class)
                );
    }

    @Test
    void createWorkout_WithInvalidExerciseDefinition_ShouldThrowException() {
        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        WorkoutRequest workoutRequest =
                new WorkoutRequest(
                        "push",
                        LocalDate.of(2025, 2, 10),
                        List.of(
                                new WorkoutExerciseRequest(
                                        1L,
                                        List.of(
                                                new SetRequest(
                                                        60.0,
                                                        10,
                                                        2
                                                ),
                                                new SetRequest(
                                                        70.0,
                                                        8,
                                                        1
                                                )
                                        )
                                )
                        )
                );

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(
                exerciseDefinitionRepository.findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                ExerciseDefinitionNotFoundException.class,
                () -> workoutService.createWorkout(workoutRequest)
        );

        verify(userRepository)
                .findByUsername("cosmin");

        verify(exerciseDefinitionRepository)
                .findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                );

        verify(workoutExerciseRepository, never())
                .save(any(WorkoutExercise.class));

        verify(exerciseSetRepository, never())
                .save(any(ExerciseSet.class));

        verifyNoInteractions(trainingGoalService);
    }

    @Test
    void getAllWorkouts_ShouldReturnCurrentUserWorkouts() {

        when(currentUserProvider.getCurrentUsername())
                .thenReturn("cosmin");

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Workout workout = new Workout();
        workout.setId(1L);
        workout.setWorkoutName("push");
        workout.setDate(
                LocalDate.of(2025, 2, 10)
        );
        workout.setUser(user);
        workout.setWorkoutExercises(
                new ArrayList<>()
        );

        int page = 0;
        int size = 10;

        String name = "push";

        LocalDate startDate =
                LocalDate.of(2025, 2, 9);

        LocalDate endDate =
                LocalDate.of(2025, 3, 11);


        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );


        Page<Long> idPage =
                new PageImpl<>(
                        List.of(1L),
                        pageable,
                        1
                );

        when(
                workoutRepository.findFilteredPageIds(
                        "cosmin",
                        name,
                        startDate,
                        endDate,
                        pageable
                )
        ).thenReturn(idPage);


        when(
                workoutRepository
                        .findDetailedByIdsAndUserUsername(
                                List.of(1L),
                                "cosmin"
                        )
        ).thenReturn(
                List.of(workout)
        );

        PagedResponse<WorkoutResponse> response =
                workoutService.getAllWorkoutsFiltered(
                        page,
                        size,
                        name,
                        startDate,
                        endDate
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.content().size()
        );

        assertEquals(
                1L,
                response.totalElements()
        );

        WorkoutResponse workoutResponse =
                response.content().getFirst();

        assertEquals(
                1L,
                workoutResponse.id()
        );

        assertEquals(
                "push",
                workoutResponse.workoutName()
        );

        assertEquals(
                LocalDate.of(2025, 2, 10),
                workoutResponse.date()
        );

        assertTrue(
                workoutResponse
                        .exerciseResponses()
                        .isEmpty()
        );

        verify(currentUserProvider)
                .getCurrentUsername();

        verify(workoutRepository)
                .findFilteredPageIds(
                        "cosmin",
                        name,
                        startDate,
                        endDate,
                        pageable
                );

        verify(workoutRepository)
                .findDetailedByIdsAndUserUsername(
                        List.of(1L),
                        "cosmin"
                );

        verifyNoInteractions(
                workoutExerciseRepository,
                exerciseSetRepository
        );
    }

    @Test
    void deleteWorkoutById_WhenWorkoutExists_ShouldDeleteWorkout() {
        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Workout workout = new Workout();
        workout.setId(1L);
        workout.setWorkoutName("push");
        workout.setDate(LocalDate.of(2025, 2, 10));
        workout.setUser(user);

        when(
                workoutRepository.findByIdAndUserUsername(
                        1L,
                        "cosmin"
                )
        ).thenReturn(Optional.of(workout));

        workoutService.deleteWorkoutById(1L);

        verify(workoutRepository)
                .findByIdAndUserUsername(
                        1L,
                        "cosmin"
                );

        verify(workoutRepository)
                .delete(workout);
    }

    @Test
    void changeOneSet_ShouldUpdateOnlyProvidedFields() {
        UpdateExerciseSetRequest request =
                new UpdateExerciseSetRequest(
                        null,
                        3,
                        null
                );

        Long workoutId = 1L;
        Integer exerciseNumber = 1;
        Integer setNumber = 1;

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(1L);
        exerciseSet.setReps(10);
        exerciseSet.setRir(9);
        exerciseSet.setSetNumber(setNumber);
        exerciseSet.setWeight(100.0);

        ExerciseDefinition exerciseDefinition =
                new ExerciseDefinition();

        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench");
        exerciseDefinition.setMuscleGroup(
                MuscleGroup.CHEST
        );

        WorkoutExercise workoutExercise =
                new WorkoutExercise();

        workoutExercise.setId(1L);
        workoutExercise.setExerciseNumber(
                exerciseNumber
        );
        workoutExercise.setExerciseDefinition(
                exerciseDefinition
        );
        workoutExercise.setExerciseSets(
                List.of(exerciseSet)
        );

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setWorkoutName("push");
        workout.setDate(LocalDate.of(2026, 7, 15));
        workout.setWorkoutExercises(
                List.of(workoutExercise)
        );

        workoutExercise.setWorkout(workout);
        exerciseSet.setWorkoutExercise(workoutExercise);

        when(
                workoutRepository.findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                )
        ).thenReturn(Optional.of(workout));

        WorkoutResponse response =
                workoutService.changeOneSet(
                        request,
                        workoutId,
                        exerciseNumber,
                        setNumber
                );

        assertNotNull(response);

        assertEquals(3, exerciseSet.getReps());
        assertEquals(
                100.0,
                exerciseSet.getWeight(),
                0.001
        );
        assertEquals(9, exerciseSet.getRir());

        SetResponse responseSet =
                response.exerciseResponses()
                        .getFirst()
                        .setResponses()
                        .getFirst();

        assertEquals(3, responseSet.reps());
        assertEquals(100.0, responseSet.weight(), 0.001);
        assertEquals(9, responseSet.rir());

        verify(workoutRepository)
                .findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                );

        verify(trainingGoalService)
                .completeGoalsFromWorkout(workout);

        verifyNoInteractions(
                workoutExerciseRepository,
                exerciseSetRepository
        );
    }

    @Test
    void changeWorkoutExercise_ShouldChangeExerciseDefinition() {
        Long workoutId = 1L;
        Integer exerciseNumber = 1;
        Long newExerciseDefinitionId = 2L;

        ChangeWorkoutExerciseRequest request =
                new ChangeWorkoutExerciseRequest(
                        newExerciseDefinitionId
                );

        ExerciseDefinition oldExerciseDefinition =
                new ExerciseDefinition();

        oldExerciseDefinition.setId(1L);
        oldExerciseDefinition.setName("Bench Press");
        oldExerciseDefinition.setMuscleGroup(
                MuscleGroup.CHEST
        );

        ExerciseDefinition newExerciseDefinition =
                new ExerciseDefinition();

        newExerciseDefinition.setId(
                newExerciseDefinitionId
        );
        newExerciseDefinition.setName(
                "Incline Bench Press"
        );
        newExerciseDefinition.setMuscleGroup(
                MuscleGroup.CHEST
        );

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setWorkoutName("Push");
        workout.setDate(LocalDate.of(2026, 7, 15));

        WorkoutExercise workoutExercise =
                new WorkoutExercise();

        workoutExercise.setId(1L);
        workoutExercise.setExerciseNumber(
                exerciseNumber
        );
        workoutExercise.setExerciseDefinition(
                oldExerciseDefinition
        );
        workoutExercise.setWorkout(workout);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(1L);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(100.0);
        exerciseSet.setReps(5);
        exerciseSet.setRir(1);
        exerciseSet.setWorkoutExercise(
                workoutExercise
        );

        workoutExercise.setExerciseSets(
                List.of(exerciseSet)
        );

        workout.setWorkoutExercises(
                List.of(workoutExercise)
        );

        when(
                workoutRepository.findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                )
        ).thenReturn(Optional.of(workout));

        when(
                exerciseDefinitionRepository.findByIdAccessible(
                        newExerciseDefinitionId,
                        "cosmin",
                        ExerciseType.SYSTEM
                )
        ).thenReturn(Optional.of(newExerciseDefinition));

        WorkoutResponse response =
                workoutService.changeWorkoutExercise(
                        request,
                        workoutId,
                        exerciseNumber
                );

        assertNotNull(response);

        assertEquals(
                newExerciseDefinitionId,
                workoutExercise
                        .getExerciseDefinition()
                        .getId()
        );

        assertEquals(
                "Incline Bench Press",
                workoutExercise
                        .getExerciseDefinition()
                        .getName()
        );

        WorkoutExerciseResponse responseExercise =
                response.exerciseResponses().getFirst();

        assertEquals(
                "Incline Bench Press",
                responseExercise.exerciseName()
        );

        assertEquals(
                exerciseNumber,
                responseExercise.exerciseNumber()
        );

        assertEquals(
                1,
                responseExercise.setResponses().size()
        );

        assertEquals(
                100.0,
                responseExercise
                        .setResponses()
                        .getFirst()
                        .weight(),
                0.001
        );

        verify(workoutRepository)
                .findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                );

        verify(exerciseDefinitionRepository)
                .findByIdAccessible(
                        newExerciseDefinitionId,
                        "cosmin",
                        ExerciseType.SYSTEM
                );

        verify(trainingGoalService)
                .completeGoalsFromWorkout(workout);

        verifyNoInteractions(
                workoutExerciseRepository,
                exerciseSetRepository
        );
    }

    @Test
    void duplicateWorkout_ShouldCopyWorkout() {
        Long workoutId = 1L;

        DuplicateWorkoutRequest request =
                new DuplicateWorkoutRequest(
                        LocalDate.of(2026, 7, 16),
                        "New Push"
                );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition =
                new ExerciseDefinition();

        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(
                MuscleGroup.CHEST
        );
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setExerciseType(
                ExerciseType.SYSTEM
        );

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setWorkoutName("Push");
        workout.setDate(LocalDate.of(2026, 7, 15));
        workout.setUser(user);

        WorkoutExercise workoutExercise =
                new WorkoutExercise();

        workoutExercise.setId(1L);
        workoutExercise.setExerciseNumber(1);
        workoutExercise.setExerciseDefinition(
                exerciseDefinition
        );
        workoutExercise.setWorkout(workout);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(1L);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(100.0);
        exerciseSet.setReps(5);
        exerciseSet.setRir(1);
        exerciseSet.setWorkoutExercise(
                workoutExercise
        );

        workoutExercise.setExerciseSets(
                List.of(exerciseSet)
        );

        workout.setWorkoutExercises(
                List.of(workoutExercise)
        );

        when(
                workoutRepository.findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                )
        ).thenReturn(Optional.of(workout));

        when(
                exerciseDefinitionRepository.findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                )
        ).thenReturn(Optional.of(exerciseDefinition));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> {
                    Workout savedWorkout =
                            invocation.getArgument(0);

                    savedWorkout.setId(2L);

                    return savedWorkout;
                });

        WorkoutResponse response =
                workoutService.duplicateWorkout(
                        request,
                        workoutId
                );

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("New Push", response.workoutName());

        assertEquals(
                LocalDate.of(2026, 7, 16),
                response.date()
        );

        assertEquals("Push", workout.getWorkoutName());

        assertEquals(
                LocalDate.of(2026, 7, 15),
                workout.getDate()
        );

        assertEquals(
                1,
                response.exerciseResponses().size()
        );

        WorkoutExerciseResponse exerciseResponse =
                response.exerciseResponses().getFirst();

        assertEquals(
                "Bench Press",
                exerciseResponse.exerciseName()
        );

        assertEquals(
                1,
                exerciseResponse.exerciseNumber()
        );

        assertEquals(
                1,
                exerciseResponse.setResponses().size()
        );

        assertEquals(
                100.0,
                exerciseResponse
                        .setResponses()
                        .getFirst()
                        .weight(),
                0.001
        );

        assertNotEquals(workout.getId(), response.id());

        verify(workoutRepository)
                .findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                );

        verify(exerciseDefinitionRepository)
                .findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                );

        verify(workoutRepository)
                .save(any(Workout.class));

        verify(trainingGoalService)
                .completeGoalsFromWorkout(
                        any(Workout.class)
                );

        verifyNoInteractions(
                workoutExerciseRepository,
                exerciseSetRepository
        );
    }

    @Test
    void duplicateWorkout_ShouldReturnNotFoundWhenNoWorkoutExists() {
        Long workoutId = 1L;

        DuplicateWorkoutRequest request =
                new DuplicateWorkoutRequest(
                        LocalDate.of(2026, 7, 5),
                        "Push"
                );

        when(
                workoutRepository.findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                WorkoutNotFoundException.class,
                () -> workoutService.duplicateWorkout(
                        request,
                        workoutId
                )
        );

        verify(workoutRepository)
                .findDetailedByIdAndUserUsername(
                        workoutId,
                        "cosmin"
                );

        verify(workoutRepository, never())
                .save(any(Workout.class));

        verifyNoInteractions(
                exerciseDefinitionRepository,
                workoutExerciseRepository,
                exerciseSetRepository,
                trainingGoalService
        );
    }
}