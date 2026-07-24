package com.cosmin.fitness_tracker_api.ServiceTest;


import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.Exception.*;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.TrainingGoal;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.TrainingGoalRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import com.cosmin.fitness_tracker_api.Service.TrainingGoalService;
import org.junit.jupiter.api.AfterEach;
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

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TrainingGoalServiceTest {

    @Mock
    TrainingGoalRepository trainingGoalRepository;

    @Mock
    ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TrainingGoalService trainingGoalService;

    @Test
    void createTrainingGoal_WithValidRequest_Should_Create_TrainingGoal() {

        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        TrainingGoalRequest trainingGoalRequest = new TrainingGoalRequest(
                1L,
                100.00,
                5,
                LocalDate.of(2027,5,6)
        );

    when(userRepository.findByUsername("cosmin")).thenReturn(Optional.of(user));


    when(exerciseDefinitionRepository.findById(1L)).thenReturn(Optional.of(exerciseDefinition));
    when(trainingGoalRepository.existsByUserUsernameAndExerciseDefinitionIdAndStatus("cosmin",1L, Status.ACTIVE))
            .thenReturn(false);

    TrainingGoal trainingGoal = new  TrainingGoal();
    trainingGoal.setExerciseDefinition(exerciseDefinition);
    trainingGoal.setUser(user);
    trainingGoal.setCreatedAt(LocalDate.now());
    trainingGoal.setTargetWeight(100.00);
    trainingGoal.setTargetReps(5);
    trainingGoal.setStatus(Status.ACTIVE);
    trainingGoal.setId(1L);

        when(trainingGoalRepository.save(any(TrainingGoal.class)))
                .thenAnswer(invocation -> {
                    TrainingGoal savedGoal = invocation.getArgument(0);
                    savedGoal.setId(1L);
                    return savedGoal;
                });


    TrainingGoalResponse response = trainingGoalService.createTrainingGoal(trainingGoalRequest);

        assertEquals(1L,response.id());
        assertEquals(100.00,response.targetWeight());
        assertEquals(5,response.targetReps());
        assertEquals(Status.ACTIVE,response.status());
        assertEquals("Bench Press",response.exerciseName());


        verify(userRepository).findByUsername("cosmin");
        verify(exerciseDefinitionRepository).findById(1L);
    }


    @Test
    void createTrainingGoal_WhenExerciseDefinitionDoesNotExist_ShouldThrowExerciseDefinitionNotFoundException() {
        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        TrainingGoalRequest request = new TrainingGoalRequest(
                99L,
                100.0,
                5,
                LocalDate.now().plusMonths(1)
        );

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ExerciseDefinitionNotFoundException.class,
                () -> trainingGoalService.createTrainingGoal(request)
        );

        verify(exerciseDefinitionRepository).findById(99L);
        verify(trainingGoalRepository, never())
                .save(any(TrainingGoal.class));
    }

    @Test
    void createTrainingGoal_WhenActiveGoalAlreadyExists_ShouldThrowActiveTrainingGoalAlreadyExistsException() {
        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setId(1L);
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        TrainingGoalRequest request = new TrainingGoalRequest(
                1L,
                100.0,
                5,
                LocalDate.now().plusMonths(1)
        );

        when(userRepository.findByUsername("cosmin"))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository.findById(1L))
                .thenReturn(Optional.of(exerciseDefinition));

        when(trainingGoalRepository
                .existsByUserUsernameAndExerciseDefinitionIdAndStatus(
                        "cosmin",
                        1L,
                        Status.ACTIVE
                ))
                .thenReturn(true);

        assertThrows(
                ActiveTrainingGoalAlreadyExistsException.class,
                () -> trainingGoalService.createTrainingGoal(request)
        );

        verify(trainingGoalRepository, never())
                .save(any(TrainingGoal.class));
    }



    @Test
    void getTrainingGoals_WithValidPageSize_Should_Return_TrainingGoals() {
        mockAuthenticatedUser();

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        int page =0;
        int size = 2;

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        ExerciseDefinition exerciseDefinition2 = new ExerciseDefinition();
        exerciseDefinition2.setName("Lat Pulldown");
        exerciseDefinition2.setMuscleGroup(MuscleGroup.BACK);

        TrainingGoal trainingGoal = new TrainingGoal();
        trainingGoal.setExerciseDefinition(exerciseDefinition);
        trainingGoal.setUser(user);
        trainingGoal.setCreatedAt(LocalDate.now());
        trainingGoal.setTargetWeight(100.00);
        trainingGoal.setTargetReps(5);
        trainingGoal.setStatus(Status.ACTIVE);
        trainingGoal.setId(1L);

        TrainingGoal trainingGoal2 = new TrainingGoal();
        trainingGoal2.setExerciseDefinition(exerciseDefinition2);
        trainingGoal2.setUser(user);
        trainingGoal2.setCreatedAt(LocalDate.now());
        trainingGoal2.setTargetWeight(100.00);
        trainingGoal2.setTargetReps(10);
        trainingGoal2.setStatus(Status.ACTIVE);

        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingGoal> trainingGoalPage = new PageImpl<>(
                List.of(trainingGoal,trainingGoal2),
                pageable,
                2
        );

        when(trainingGoalRepository.findByUserUsernameOrderByIdAsc("cosmin",pageable)).thenReturn(trainingGoalPage);

        PagedResponse<TrainingGoalResponse> response = trainingGoalService.getTrainingGoals(page,size);

        TrainingGoalResponse response1 = response.content().getFirst();
        TrainingGoalResponse response2 = response.content().getLast();

        assertEquals(2,response.totalElements());

        assertEquals("Bench Press",response1.exerciseName());
        assertEquals(Status.ACTIVE,response1.status());
        assertEquals(5,response1.targetReps());
        assertEquals(100.00,response1.targetWeight());

        assertEquals("Lat Pulldown",response2.exerciseName());
        assertEquals(Status.ACTIVE,response2.status());
        assertEquals(10,response2.targetReps());
        assertEquals(100.00,response2.targetWeight());


        verify(trainingGoalRepository).findByUserUsernameOrderByIdAsc("cosmin",pageable);
    }

    @Test
    void cancelTrainingGoal_WithValidIndex_Should_Cancel_TrainingGoal() {
        mockAuthenticatedUser();
        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");

        Long trainingGoalId = 1L;

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.BACK);

        TrainingGoal trainingGoal = new TrainingGoal();
        trainingGoal.setExerciseDefinition(exerciseDefinition);
        trainingGoal.setUser(user);
        trainingGoal.setCreatedAt(LocalDate.now());
        trainingGoal.setTargetWeight(100.00);
        trainingGoal.setTargetReps(5);
        trainingGoal.setStatus(Status.ACTIVE);
        trainingGoal.setTargetDate(LocalDate.now().plusDays(1));
        trainingGoal.setId(1L);

        when(trainingGoalRepository.findByUserUsernameAndId("cosmin",trainingGoalId)).thenReturn(Optional.of(trainingGoal));



        TrainingGoalResponse response = trainingGoalService.cancelTrainingGoal(trainingGoalId);

        assertEquals(Status.CANCELLED,response.status());
        assertEquals(5,response.targetReps());
        assertEquals(100.00,response.targetWeight());

        verify(trainingGoalRepository).findByUserUsernameAndId("cosmin",trainingGoalId);
    }

    @Test
    void cancelTrainingGoal_WhenGoalDoesNotExist_ShouldThrowTrainingGoalNotFoundException() {
        mockAuthenticatedUser();

        when(trainingGoalRepository.findByUserUsernameAndId("cosmin", 99L))
                .thenReturn(Optional.empty());

        assertThrows(
                TrainingGoalNotFoundException.class,
                () -> trainingGoalService.cancelTrainingGoal(99L)
        );

        verify(trainingGoalRepository)
                .findByUserUsernameAndId("cosmin", 99L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
