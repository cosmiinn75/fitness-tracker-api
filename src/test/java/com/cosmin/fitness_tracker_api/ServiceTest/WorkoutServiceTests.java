package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.ExerciseRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseResponse;
import com.cosmin.fitness_tracker_api.DTO.SetRequest;
import com.cosmin.fitness_tracker_api.DTO.SetResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutResponse;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        mockAuthenticatedUser("cosmin");

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

        ExerciseResponse exerciseResponse = response.exerciseResponses().get(0);

        assertEquals(1L, exerciseResponse.id());
        assertEquals(1, exerciseResponse.exerciseNumber());
        assertEquals("Bench Press", exerciseResponse.exerciseName());
        assertEquals(2, exerciseResponse.setResponses().size());

        SetResponse firstSet = exerciseResponse.setResponses().get(0);

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

        mockAuthenticatedUser("cosmin");

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

        mockAuthenticatedUser("cosmin");

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Workout workout = new Workout();
        workout.setId(1L);
        workout.setWorkoutName("push");
        workout.setDate(LocalDate.of(2025, 2, 10));
        workout.setUser(user);

        when(workoutRepository.findByUserUsernameOrderByDateDesc("cosmin"))
                .thenReturn(List.of(workout));

        when(workoutExerciseRepository.findByWorkoutOrderByExerciseNumberAsc(workout))
                .thenReturn(new ArrayList<>());


        List<WorkoutResponse> response = workoutService.getAllWorkouts();


        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals("push", response.get(0).workoutName());
        assertEquals(LocalDate.of(2025, 2, 10), response.get(0).date());
        assertEquals(0, response.get(0).exerciseResponses().size());

        verify(workoutRepository).findByUserUsernameOrderByDateDesc("cosmin");
        verify(workoutExerciseRepository).findByWorkoutOrderByExerciseNumberAsc(workout);
    }

    @Test
    void deleteWorkoutById_WhenWorkoutExists_ShouldDeleteWorkout() {

        mockAuthenticatedUser("cosmin");

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

    


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String username) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}