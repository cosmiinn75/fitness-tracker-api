package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.WorkoutNotFoundException;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Model.Workout;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutExerciseRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutRepository;
import com.cosmin.fitness_tracker_api.Service.WorkoutService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTests {

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

    @InjectMocks
    private WorkoutService workoutService;

    @Test
    void createWorkout_WithValidData_ShouldCreateWorkout() {
        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");

        WorkoutRequest workoutRequest = new WorkoutRequest(
                "push",
                LocalDate.of(2025, 2, 10),
                List.of(
                        new ExerciseRequest(
                                1L,
                                List.of(
                                        new SetRequest(60.0, 10, 2),
                                        new SetRequest(70.0, 8, 1)
                                )
                        )
                )
        );

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository.findById(1L))
                .thenReturn(Optional.of(exerciseDefinition));

        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> {
                    Workout workout = invocation.getArgument(0);
                    workout.setId(1L);
                    return workout;
                });

        when(workoutExerciseRepository.save(any(WorkoutExercise.class)))
                .thenAnswer(invocation -> {
                    WorkoutExercise workoutExercise = invocation.getArgument(0);
                    workoutExercise.setId(1L);
                    return workoutExercise;
                });

        when(exerciseSetRepository.save(any(ExerciseSet.class)))
                .thenAnswer(invocation -> {
                    ExerciseSet exerciseSet = invocation.getArgument(0);
                    exerciseSet.setId(1L);
                    return exerciseSet;
                });


        WorkoutResponse response = workoutService.createWorkout(workoutRequest);


        assertEquals(1L, response.id());
        assertEquals("push", response.workoutName());
        assertEquals(LocalDate.of(2025, 2, 10), response.date());
        assertEquals(1, response.exerciseResponses().size());

        ExerciseResponse exerciseResponse = response.exerciseResponses().getFirst();

        assertEquals(1L, exerciseResponse.id());
        assertEquals(1, exerciseResponse.exerciseNumber());
        assertEquals("Bench Press", exerciseResponse.exerciseName());
        assertEquals(2, exerciseResponse.setResponses().size());

        SetResponse firstSet = exerciseResponse.setResponses().getFirst();

        assertEquals(1L, firstSet.id());
        assertEquals(1, firstSet.setNumber());
        assertEquals(60.0, firstSet.weight());
        assertEquals(10, firstSet.reps());
        assertEquals(2, firstSet.rir());

        verify(userRepository).findByUsername("cosmin");
        verify(exerciseDefinitionRepository).findById(1L);
        verify(workoutRepository).save(any(Workout.class));
        verify(workoutExerciseRepository).save(any(WorkoutExercise.class));
        verify(exerciseSetRepository, times(2)).save(any(ExerciseSet.class));
    }

    @Test
    void createWorkout_WithInvalidExerciseDefinition_ShouldThrowException() {

        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        WorkoutRequest workoutRequest = new WorkoutRequest(
                "push",
                LocalDate.of(2025, 2, 10),
                List.of(
                        new ExerciseRequest(
                                1L,
                                List.of(
                                        new SetRequest(60.0, 10, 2),
                                        new SetRequest(70.0, 8, 1)
                                )
                        )
                )
        );

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ExerciseDefinitionNotFoundException.class,
                () -> workoutService.createWorkout(workoutRequest)
        );

        verify(userRepository).findByUsername("cosmin");
        verify(exerciseDefinitionRepository).findById(1L);

        verify(workoutExerciseRepository, never()).save(any(WorkoutExercise.class));
        verify(exerciseSetRepository, never()).save(any(ExerciseSet.class));
    }



    @Test
    void getAllWorkouts_ShouldReturnCurrentUserWorkouts() {

        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Workout workout = new Workout();
        workout.setId(1L);
        workout.setWorkoutName("push");
        workout.setDate(LocalDate.of(2025, 2, 10));
        workout.setUser(user);

        int page = 0;
        int size = 10;

        String name = "push";
        LocalDate startDate = LocalDate.of(2025, 2, 9);
        LocalDate endDate = LocalDate.of(2025, 3, 11);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Workout> workoutPage = new PageImpl<>(
                List.of(workout),
                pageable,
                1
        );

        when(workoutRepository.findFilteredWorkouts(
                "cosmin",
                name,
                startDate,
                endDate,
                pageable
        )).thenReturn(workoutPage);

        when(workoutExerciseRepository
                .findByWorkoutOrderByExerciseNumberAsc(workout))
                .thenReturn(new ArrayList<>());

        PagedResponse<WorkoutResponse> response =
                workoutService.getAllWorkoutsFiltered(
                        page,
                        size,
                        name,
                        startDate,
                        endDate
                );

        assertEquals(1, response.content().size());
        assertEquals(1L, response.content().getFirst().id());
        assertEquals("push", response.content().getFirst().workoutName());
        assertEquals(
                LocalDate.of(2025, 2, 10),
                response.content().getFirst().date()
        );
        assertTrue(
                response.content().getFirst().exerciseResponses().isEmpty()
        );

        verify(workoutRepository).findFilteredWorkouts(
                "cosmin",
                name,
                startDate,
                endDate,
                pageable
        );

        verify(workoutExerciseRepository)
                .findByWorkoutOrderByExerciseNumberAsc(workout);
    }

    @Test
    void deleteWorkoutById_WhenWorkoutExists_ShouldDeleteWorkout() {

        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Workout workout = new Workout();
        workout.setId(1L);
        workout.setWorkoutName("push");
        workout.setDate(LocalDate.of(2025, 2, 10));
        workout.setUser(user);

        when(workoutRepository.findByIdAndUserUsername(1L, "cosmin"))
                .thenReturn(Optional.of(workout));


        workoutService.deleteWorkoutById(1L);


        verify(workoutRepository).findByIdAndUserUsername(1L, "cosmin");
        verify(workoutRepository).delete(workout);
    }

    @Test
    void changeOneSet_ShouldUpdateOnlyProvidedFields(){

        mockAuthenticatedUser();

        UpdateExerciseSetRequest  request = new UpdateExerciseSetRequest(
                null,
                3,
                null
        );

        Long workoutId = 1L;
        Integer exerciseNumber = 1;
        Integer setNumber = 1;

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setReps(10);
        exerciseSet.setRir(9);
        exerciseSet.setSetNumber(setNumber);
        exerciseSet.setWeight(100.00);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setExerciseNumber(exerciseNumber);
        workoutExercise.setExerciseSets(List.of(exerciseSet));
        workoutExercise.setExerciseDefinition(exerciseDefinition);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setWorkoutName("push");
        workout.setWorkoutExercises(List.of(workoutExercise));

        when(workoutRepository.findByIdAndUserUsername(1L, "cosmin"))
                .thenReturn(Optional.of(workout));

        when(workoutExerciseRepository
                .findByWorkoutOrderByExerciseNumberAsc(workout))
                .thenReturn(List.of(workoutExercise));

        when(exerciseSetRepository
                .findByWorkoutExerciseOrderBySetNumberAsc(workoutExercise))
                .thenReturn(List.of(exerciseSet));


        WorkoutResponse response = workoutService.changeOneSet(request,workoutId,exerciseNumber,setNumber);

        assertEquals(3, exerciseSet.getReps());
        assertEquals(100.0, exerciseSet.getWeight(), 0.001);
        assertEquals(9, exerciseSet.getRir());

        assertNotNull(response);

        verify(workoutRepository)
                .findByIdAndUserUsername(workoutId, "cosmin");
    }
    @Test
    void changeWorkoutExercise_ShouldChangeExerciseDefinition() {
        mockAuthenticatedUser();

        Long workoutId = 1L;
        Integer exerciseNumber = 1;
        Long newExerciseDefinitionId = 2L;

        ChangeWorkoutExerciseRequest request =
                new ChangeWorkoutExerciseRequest(newExerciseDefinitionId);

        ExerciseDefinition oldExerciseDefinition = new ExerciseDefinition();
        oldExerciseDefinition.setId(1L);
        oldExerciseDefinition.setName("Bench Press");
        oldExerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        ExerciseDefinition newExerciseDefinition = new ExerciseDefinition();
        newExerciseDefinition.setId(newExerciseDefinitionId);
        newExerciseDefinition.setName("Incline Bench Press");
        newExerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setWorkoutName("Push");
        workout.setDate(LocalDate.of(2026, 7, 15));

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(1L);
        workoutExercise.setExerciseNumber(exerciseNumber);
        workoutExercise.setExerciseDefinition(oldExerciseDefinition);
        workoutExercise.setWorkout(workout);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(1L);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(100.0);
        exerciseSet.setReps(5);
        exerciseSet.setRir(1);
        exerciseSet.setWorkoutExercise(workoutExercise);

        workoutExercise.setExerciseSets(List.of(exerciseSet));
        workout.setWorkoutExercises(List.of(workoutExercise));

        when(workoutRepository.findByIdAndUserUsername(workoutId, "cosmin"))
                .thenReturn(Optional.of(workout));

        when(exerciseDefinitionRepository.findById(newExerciseDefinitionId))
                .thenReturn(Optional.of(newExerciseDefinition));

        when(workoutExerciseRepository
                .findByWorkoutOrderByExerciseNumberAsc(workout))
                .thenReturn(List.of(workoutExercise));

        when(exerciseSetRepository
                .findByWorkoutExerciseOrderBySetNumberAsc(workoutExercise))
                .thenReturn(List.of(exerciseSet));

        WorkoutResponse response = workoutService.changeWorkoutExercise(
                request,
                workoutId,
                exerciseNumber
        );

        assertNotNull(response);

        assertEquals(
                newExerciseDefinitionId,
                workoutExercise.getExerciseDefinition().getId()
        );

        assertEquals(
                "Incline Bench Press",
                workoutExercise.getExerciseDefinition().getName()
        );


        assertEquals(1, workoutExercise.getExerciseSets().size());
        assertEquals(100.0, exerciseSet.getWeight(), 0.001);
        assertEquals(5, exerciseSet.getReps());
        assertEquals(1, exerciseSet.getRir());


        assertEquals(exerciseNumber, workoutExercise.getExerciseNumber());

        verify(workoutRepository)
                .findByIdAndUserUsername(workoutId, "cosmin");

        verify(exerciseDefinitionRepository)
                .findById(newExerciseDefinitionId);
    }

    @Test
    void duplicateWorkout_ShouldCopyWorkout(){
        mockAuthenticatedUser();
        Long workoutId = 1L;

        DuplicateWorkoutRequest request = new DuplicateWorkoutRequest(
                LocalDate.of(2026,7,16),
                "New Push"
        );

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        Workout workout = new Workout();
        workout.setId(workoutId);
        workout.setWorkoutName("Push");
        workout.setDate(LocalDate.of(2026, 7, 15));

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setId(1L);
        workoutExercise.setExerciseDefinition(exerciseDefinition);
        workoutExercise.setWorkout(workout);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(1L);
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(100.0);
        exerciseSet.setReps(5);
        exerciseSet.setRir(1);
        exerciseSet.setWorkoutExercise(workoutExercise);

        workoutExercise.setExerciseSets(List.of(exerciseSet));
        workout.setWorkoutExercises(List.of(workoutExercise));

        when(workoutRepository.findByIdAndUserUsername(workoutId, "cosmin"))
                .thenReturn(Optional.of(workout));

        when(workoutRepository.save(any(Workout.class))).
        thenAnswer(invocation -> {
            Workout savedWorkout = invocation.getArgument(0);
            savedWorkout.setId(2L);
            return savedWorkout;
        });

        WorkoutResponse response = workoutService.duplicateWorkout(request,workoutId);

        assertNotNull(response);
        assertNotNull(response.id());

        assertEquals("New Push",response.workoutName());
        assertEquals(LocalDate.of(2026, 7, 16), response.date());
        assertEquals("Push",workout.getWorkoutName());
        assertEquals(LocalDate.of(2026, 7, 15), workout.getDate());

        assertNotEquals(workout.getId(), response.id());

        verify(workoutRepository).findByIdAndUserUsername(workoutId, "cosmin");
        verify(workoutRepository).save(any(Workout.class));
    }

    @Test
    void duplicateWorkout_ShouldReturnNotFoundWheNoWorkout(){
        mockAuthenticatedUser();
        Long workoutId = 1L;

        DuplicateWorkoutRequest request = new DuplicateWorkoutRequest(
                LocalDate.of(2026,7,5),
                "Push"
        );

        when(workoutRepository.findByIdAndUserUsername(workoutId, "cosmin"))
                .thenReturn(Optional.empty());

        assertThrows(WorkoutNotFoundException.class, () -> workoutService.duplicateWorkout(request,workoutId));

    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        "cosmin",
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}