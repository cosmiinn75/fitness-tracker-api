package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Exception.DuplicateExerciseDefinitionException;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.NameAlreadyExistsException;
import com.cosmin.fitness_tracker_api.Exception.WorkoutTemplateNotFoundException;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.*;
import com.cosmin.fitness_tracker_api.Service.WorkoutTemplateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutTemplateServiceTest {

    @Mock
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Mock
    private WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;

    @Mock
    private  WorkoutTemplateSetRepository workoutTemplateSetRepository;

    @Mock
    private  ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Mock
    private  UserRepository userRepository;

    @InjectMocks
    private WorkoutTemplateService workoutTemplateService;



    @Test
    void createWorkoutTemplate_shouldCreateTemplateWithExercisesAndSets() {
        mockAuthenticatedUser();

        WorkoutTemplateSetRequest setRequest =
                new WorkoutTemplateSetRequest(
                        100.00,
                        5,
                        2
                );

        WorkoutTemplateExerciseRequest exerciseRequest =
                new WorkoutTemplateExerciseRequest(
                        1L,
                        List.of(setRequest)
                );

        WorkoutTemplateRequest request =
                new WorkoutTemplateRequest(
                        "Push Day",
                        List.of(exerciseRequest)
                );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setNormalizedName("bench press");

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository
                .findByUserUsernameAndNormalizedName("cosmin", "push day"))
                .thenReturn(Optional.empty());

        when(exerciseDefinitionRepository.findByIdAccessible(
                1L,
                "cosmin",
                ExerciseType.SYSTEM
        )).thenReturn(Optional.of(exerciseDefinition));

        when(workoutTemplateRepository.save(any(WorkoutTemplate.class)))
                .thenAnswer(invocation -> {
                    WorkoutTemplate template = invocation.getArgument(0);
                    template.setId(1L);
                    return template;
                });

        when(workoutTemplateExerciseRepository.save(
                any(WorkoutTemplateExercise.class)
        )).thenAnswer(invocation -> {
            WorkoutTemplateExercise exercise = invocation.getArgument(0);
            exercise.setId(1L);
            return exercise;
        });

        when(workoutTemplateSetRepository.save(any(WorkoutTemplateSet.class)))
                .thenAnswer(invocation -> {
                    WorkoutTemplateSet set = invocation.getArgument(0);
                    set.setId(1L);
                    return set;
                });

        WorkoutTemplateResponse response =
                workoutTemplateService.createWorkoutTemplate(request);

        assertEquals(1L, response.id());
        assertEquals("Push Day", response.workoutName());
        assertEquals(1, response.templateExercises().size());

        WorkoutTemplateExerciseResponse exerciseResponse =
                response.templateExercises().getFirst();

        assertEquals(1L, exerciseResponse.exerciseDefinitionId());
        assertEquals("Bench Press", exerciseResponse.exerciseName());
        assertEquals(1, exerciseResponse.exerciseNumber());
        assertEquals(1, exerciseResponse.templateSets().size());

        WorkoutTemplateSetResponse setResponse =
                exerciseResponse.templateSets().getFirst();

        assertEquals(1, setResponse.setNumber());
        assertEquals(100.00, setResponse.targetWeight());
        assertEquals(5, setResponse.targetReps());
        assertEquals(2, setResponse.targetRir());

        verify(userRepository).findByUsername("cosmin");

        verify(workoutTemplateRepository)
                .findByUserUsernameAndNormalizedName(
                        "cosmin",
                        "push day"
                );

        verify(exerciseDefinitionRepository)
                .findByIdAccessible(
                        1L,
                        "cosmin",
                        ExerciseType.SYSTEM
                );

        verify(workoutTemplateRepository)
                .save(any(WorkoutTemplate.class));

        verify(workoutTemplateExerciseRepository)
                .save(any(WorkoutTemplateExercise.class));

        verify(workoutTemplateSetRepository)
                .save(any(WorkoutTemplateSet.class));
    }


    @Test
    void createWorkoutTemplate_shouldThrowWhenNormalizedNameAlreadyExists() {

        mockAuthenticatedUser();

        WorkoutTemplateSetRequest setRequest =
                new WorkoutTemplateSetRequest(
                        100.00,
                        5,
                        2
                );

        WorkoutTemplateExerciseRequest exerciseRequest =
                new WorkoutTemplateExerciseRequest(
                        1L,
                        List.of(setRequest)
                );

        WorkoutTemplateRequest request =
                new WorkoutTemplateRequest(
                        "Push Day",
                        List.of(exerciseRequest)
                );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        WorkoutTemplate template = new WorkoutTemplate();
        template.setNormalizedName("push day");
        template.setUser(user);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setNormalizedName("bench press");

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository
                .findByUserUsernameAndNormalizedName("cosmin", "push day"))
                .thenReturn(Optional.of(template));

        assertThrows(NameAlreadyExistsException.class,
                () -> workoutTemplateService.createWorkoutTemplate(request));

        verify(workoutTemplateRepository, never())
                .save(any(WorkoutTemplate.class));


    }

    @Test
    void createWorkoutTemplate_shouldThrowWhenExerciseDefinitionIsDuplicated() {
        mockAuthenticatedUser();

        WorkoutTemplateSetRequest setRequest =
                new WorkoutTemplateSetRequest(100.0, 5, 2);

        WorkoutTemplateExerciseRequest firstExercise =
                new WorkoutTemplateExerciseRequest(
                        1L,
                        List.of(setRequest)
                );

        WorkoutTemplateExerciseRequest duplicatedExercise =
                new WorkoutTemplateExerciseRequest(
                        1L,
                        List.of(setRequest)
                );

        WorkoutTemplateRequest request =
                new WorkoutTemplateRequest(
                        "Push Day",
                        List.of(firstExercise, duplicatedExercise)
                );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setArchived(false);

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository
                .findByUserUsernameAndNormalizedName(
                        "cosmin",
                        "push day"
                ))
                .thenReturn(Optional.empty());

        when(workoutTemplateRepository.save(any(WorkoutTemplate.class)))
                .thenAnswer(invocation -> {
                    WorkoutTemplate template = invocation.getArgument(0);
                    template.setId(1L);
                    return template;
                });

        when(exerciseDefinitionRepository.findByIdAccessible(
                1L,
                "cosmin",
                ExerciseType.SYSTEM
        )).thenReturn(Optional.of(exerciseDefinition));

        when(workoutTemplateExerciseRepository
                .save(any(WorkoutTemplateExercise.class)))
                .thenAnswer(invocation -> {
                    WorkoutTemplateExercise exercise =
                            invocation.getArgument(0);
                    exercise.setId(1L);
                    return exercise;
                });

        when(workoutTemplateSetRepository
                .save(any(WorkoutTemplateSet.class)))
                .thenAnswer(invocation -> {
                    WorkoutTemplateSet set = invocation.getArgument(0);
                    set.setId(1L);
                    return set;
                });

        assertThrows(
                DuplicateExerciseDefinitionException.class,
                () -> workoutTemplateService.createWorkoutTemplate(request)
        );
    }

    @Test
    void createWorkoutTemplate_shouldThrowWhenExerciseDefinitionIsNotAccessible() {
        mockAuthenticatedUser();

        WorkoutTemplateSetRequest setRequest =
                new WorkoutTemplateSetRequest(100.0, 5, 2);

        WorkoutTemplateExerciseRequest exerciseRequest =
                new WorkoutTemplateExerciseRequest(
                        99L,
                        List.of(setRequest)
                );

        WorkoutTemplateRequest request =
                new WorkoutTemplateRequest(
                        "Push Day",
                        List.of(exerciseRequest)
                );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository
                .findByUserUsernameAndNormalizedName(
                        "cosmin",
                        "push day"
                ))
                .thenReturn(Optional.empty());

        when(workoutTemplateRepository.save(any(WorkoutTemplate.class)))
                .thenAnswer(invocation -> {
                    WorkoutTemplate template = invocation.getArgument(0);
                    template.setId(1L);
                    return template;
                });

        when(exerciseDefinitionRepository.findByIdAccessible(
                99L,
                "cosmin",
                ExerciseType.SYSTEM
        )).thenReturn(Optional.empty());

        assertThrows(
                ExerciseDefinitionNotFoundException.class,
                () -> workoutTemplateService.createWorkoutTemplate(request)
        );

        verify(workoutTemplateExerciseRepository, never())
                .save(any(WorkoutTemplateExercise.class));

        verify(workoutTemplateSetRepository, never())
                .save(any(WorkoutTemplateSet.class));
    }

    @Test
    void getTemplateById_shouldThrowWhenTemplateDoesNotBelongToUser() {
        mockAuthenticatedUser();

        when(workoutTemplateRepository.findByIdAndUserUsername(
                10L,
                "cosmin"
        )).thenReturn(Optional.empty());

        assertThrows(
                WorkoutTemplateNotFoundException.class,
                () -> workoutTemplateService.getTemplateById(10L)
        );

        verify(workoutTemplateRepository)
                .findByIdAndUserUsername(10L, "cosmin");

        verifyNoInteractions(
                workoutTemplateExerciseRepository,
                workoutTemplateSetRepository
        );
    }

    @Test
    void deleteTemplateById_shouldThrowWhenTemplateDoesNotBelongToUser() {
        mockAuthenticatedUser();

        when(workoutTemplateRepository.findByIdAndUserUsername(
                5L,
                "cosmin"
        )).thenReturn(Optional.empty());

        assertThrows(
                WorkoutTemplateNotFoundException.class,
                () -> workoutTemplateService.deleteTemplateById(5L)
        );

        verify(workoutTemplateRepository, never())
                .delete(any(WorkoutTemplate.class));
    }

    @Test
    void prepareWorkoutFromTemplate_shouldReturnWorkoutDraftWithoutSaving() {
        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(7L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(5L);
        template.setUser(user);
        template.setTemplateName("Push Day");
        template.setNormalizedName("push day");

        WorkoutTemplateExercise templateExercise =
                new WorkoutTemplateExercise();

        templateExercise.setId(10L);
        templateExercise.setWorkoutTemplate(template);
        templateExercise.setExerciseDefinition(exerciseDefinition);
        templateExercise.setExerciseNumber(1);

        WorkoutTemplateSet templateSet = new WorkoutTemplateSet();
        templateSet.setId(20L);
        templateSet.setWorkoutTemplateExercise(templateExercise);
        templateSet.setSetNumber(1);
        templateSet.setTargetWeight(100.0);
        templateSet.setTargetReps(5);
        templateSet.setTargetRir(2);

        template.setTemplateExercises(List.of(templateExercise));
        templateExercise.setTemplateSets(List.of(templateSet));

        when(workoutTemplateRepository.findByIdAndUserUsername(
                5L,
                "cosmin"
        )).thenReturn(Optional.of(template));

        LocalDate expectedDate = LocalDate.now();

        WorkoutRequest response =
                workoutTemplateService.prepareWorkoutFromTemplate(5L);

        assertEquals("Push Day", response.workoutName());
        assertEquals(expectedDate, response.date());
        assertEquals(1, response.exerciseRequests().size());

        WorkoutExerciseRequest exerciseResponse =
                response.exerciseRequests().getFirst();

        assertEquals(7L, exerciseResponse.exerciseDefinitionId());
        assertEquals(1, exerciseResponse.setRequests().size());

        SetRequest setResponse =
                exerciseResponse.setRequests().getFirst();

        assertEquals(100.0, setResponse.weight());
        assertEquals(5, setResponse.reps());
        assertEquals(2, setResponse.rir());

        verify(workoutTemplateRepository, never())
                .save(any(WorkoutTemplate.class));

        verify(workoutTemplateExerciseRepository, never())
                .save(any(WorkoutTemplateExercise.class));

        verify(workoutTemplateSetRepository, never())
                .save(any(WorkoutTemplateSet.class));
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
