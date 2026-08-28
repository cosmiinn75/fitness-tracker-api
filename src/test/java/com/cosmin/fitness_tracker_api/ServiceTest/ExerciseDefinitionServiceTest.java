package com.cosmin.fitness_tracker_api.ServiceTest;


import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.exception.NameAlreadyExistsException;
import com.cosmin.fitness_tracker_api.mapper.ExerciseDefinitionMapper;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import com.cosmin.fitness_tracker_api.service.ExerciseDefinitionCacheService;
import com.cosmin.fitness_tracker_api.service.ExerciseDefinitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExerciseDefinitionServiceTest {

    @Mock
    private ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ExerciseDefinitionMapper exerciseDefinitionMapper;

    @Mock
    private ExerciseDefinitionCacheService exerciseDefinitionCacheService;

    @InjectMocks
    private ExerciseDefinitionService exerciseDefinitionService;


    @Test
    void findAllExerciseDefinitions_ShouldFindAll(){

        String username = "cosmin";

        List<ExerciseDefinitionResponse> expected = List.of();

        when(currentUserProvider.getCurrentUsername()).thenReturn(username);



        when(exerciseDefinitionCacheService.findAll(username)).thenReturn(expected);

        var result = exerciseDefinitionService.findAllExerciseDefinitions();

        assertEquals(expected,result);

        verify(exerciseDefinitionCacheService).findAll(username);

    }

    @Test
    void findExerciseDefinitionById_ShouldFindExercise(){
        Long id = 1L;
        String username = "cosmin";

        ExerciseDefinitionResponse expected = new ExerciseDefinitionResponse(
                1L,
                "Bench",
                MuscleGroup.CHEST,
                ExerciseType.SYSTEM,
                false
        );

        when(currentUserProvider.getCurrentUsername()).thenReturn(username);
        when(exerciseDefinitionCacheService.findById(username,id)).thenReturn(expected);

        var result = exerciseDefinitionService.findExerciseDefinitionById(id);

        assertEquals(expected,result);
        verify(exerciseDefinitionCacheService).findById(username,id);
    }

    @Test
    void archiveExerciseDefinition_ShouldArchiveAndEvict() {
        Long id = 1L;
        String username = "cosmin";

        User user = new User();
        user.setUsername(username);

        when(currentUserProvider.getCurrentUsername())
                .thenReturn(username);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("bench");
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setId(id);
        exerciseDefinition.setNormalizedName("bench");
        exerciseDefinition.setExerciseType(ExerciseType.CUSTOM);
        exerciseDefinition.setOwner(user);

        when(exerciseDefinitionRepository
                .findByIdAndOwnerUsernameAndExerciseTypeAndArchivedFalse(
                        id,
                        username,
                        ExerciseType.CUSTOM
                ))
                .thenReturn(Optional.of(exerciseDefinition));

        exerciseDefinitionService.archiveExerciseDefinition(id);

        assertTrue(exerciseDefinition.isArchived());

        verify(exerciseDefinitionCacheService)
                .evictListAndExercise(username, id);
    }


    @Test
    void addExerciseDefinition_ShouldAddAndEvict() {

        String username = "cosmin";
        String normalizedName = "bench";

        User user = new User();
        user.setUsername(username);

        ExerciseDefinitionRequest request =
                new ExerciseDefinitionRequest(
                        "bench",
                        MuscleGroup.CHEST
                );

        when(currentUserProvider.getCurrentUsername())
                .thenReturn(username);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository
                .existsByOwnerUsernameAndNormalizedNameAndMuscleGroup(
                        username,
                        normalizedName,
                        MuscleGroup.CHEST
                ))
                .thenReturn(false);

        when(exerciseDefinitionRepository
                .existsByExerciseTypeAndNormalizedNameAndMuscleGroup(
                        ExerciseType.SYSTEM,
                        normalizedName,
                        MuscleGroup.CHEST
                ))
                .thenReturn(false);

        when(exerciseDefinitionRepository.save(any(ExerciseDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(exerciseDefinitionMapper.toResponse(any(ExerciseDefinition.class)))
                .thenAnswer(invocation -> {

                    ExerciseDefinition exerciseDefinition =
                            invocation.getArgument(0);

                    return new ExerciseDefinitionResponse(
                            exerciseDefinition.getId(),
                            exerciseDefinition.getName(),
                            exerciseDefinition.getMuscleGroup(),
                            exerciseDefinition.getExerciseType(),
                            exerciseDefinition.isArchived()
                    );
                });

        ExerciseDefinitionResponse response =
                exerciseDefinitionService.addExerciseDefinition(request);

        assertNotNull(response);
        assertEquals("bench", response.exerciseName());
        assertEquals(MuscleGroup.CHEST, response.muscleGroup());
        assertEquals(ExerciseType.CUSTOM, response.exerciseType());
        assertFalse(response.archived());

        ArgumentCaptor<ExerciseDefinition> captor =
                ArgumentCaptor.forClass(ExerciseDefinition.class);

        verify(exerciseDefinitionRepository)
                .save(captor.capture());

        ExerciseDefinition savedExercise =
                captor.getValue();

        assertEquals("bench", savedExercise.getName());
        assertEquals("bench", savedExercise.getNormalizedName());
        assertEquals(MuscleGroup.CHEST, savedExercise.getMuscleGroup());
        assertEquals(ExerciseType.CUSTOM, savedExercise.getExerciseType());
        assertFalse(savedExercise.isArchived());
        assertEquals(user, savedExercise.getOwner());

        verify(exerciseDefinitionCacheService)
                .evictList(username);
    }

    @Test
    void addExerciseDefinition_ShouldThrowWhenCustomExerciseAlreadyExists() {

        String username = "cosmin";
        String normalizedName = "bench";

        User user = new User();
        user.setUsername(username);

        ExerciseDefinitionRequest request =
                new ExerciseDefinitionRequest(
                        "bench",
                        MuscleGroup.CHEST
                );

        when(currentUserProvider.getCurrentUsername())
                .thenReturn(username);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository
                .existsByOwnerUsernameAndNormalizedNameAndMuscleGroup(
                        username,
                        normalizedName,
                        MuscleGroup.CHEST
                ))
                .thenReturn(true);

        assertThrows(
                NameAlreadyExistsException.class,
                () -> exerciseDefinitionService.addExerciseDefinition(request)
        );

        verify(exerciseDefinitionRepository, never())
                .save(any(ExerciseDefinition.class));

        verify(exerciseDefinitionCacheService, never())
                .evictList(anyString());
    }


    @Test
    void addExerciseDefinition_ShouldThrowWhenSystemExerciseAlreadyExists() {

        String username = "cosmin";
        String normalizedName = "bench";

        User user = new User();
        user.setUsername(username);

        ExerciseDefinitionRequest request =
                new ExerciseDefinitionRequest(
                        "bench",
                        MuscleGroup.CHEST
                );

        when(currentUserProvider.getCurrentUsername())
                .thenReturn(username);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(exerciseDefinitionRepository
                .existsByOwnerUsernameAndNormalizedNameAndMuscleGroup(
                        username,
                        normalizedName,
                        MuscleGroup.CHEST
                ))
                .thenReturn(false);

        when(exerciseDefinitionRepository
                .existsByExerciseTypeAndNormalizedNameAndMuscleGroup(
                        ExerciseType.SYSTEM,
                        normalizedName,
                        MuscleGroup.CHEST
                ))
                .thenReturn(true);

        assertThrows(
                NameAlreadyExistsException.class,
                () -> exerciseDefinitionService.addExerciseDefinition(request)
        );

        verify(exerciseDefinitionRepository, never())
                .save(any(ExerciseDefinition.class));

        verify(exerciseDefinitionCacheService, never())
                .evictList(anyString());
    }
}
