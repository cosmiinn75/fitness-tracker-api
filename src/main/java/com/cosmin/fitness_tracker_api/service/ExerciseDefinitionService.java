package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.exception.NameAlreadyExistsException;
import com.cosmin.fitness_tracker_api.exception.UserNotFoundException;
import com.cosmin.fitness_tracker_api.mapper.ExerciseDefinitionMapper;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ExerciseDefinitionService {

    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ExerciseDefinitionMapper exerciseDefinitionMapper;
    private final ExerciseDefinitionCacheService exerciseDefinitionCacheService;

    public ExerciseDefinitionService(
            ExerciseDefinitionRepository exerciseDefinitionRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            ExerciseDefinitionMapper exerciseDefinitionMapper,
            ExerciseDefinitionCacheService exerciseDefinitionCacheService
    ) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.exerciseDefinitionMapper = exerciseDefinitionMapper;
        this.exerciseDefinitionCacheService = exerciseDefinitionCacheService;
    }

    @Transactional
    public ExerciseDefinitionResponse addExerciseDefinition(
            ExerciseDefinitionRequest request
    ) {
        String username = currentUserProvider.getCurrentUsername();

        String cleanedName = request.exerciseName()
                .strip()
                .replaceAll("\\s+", " ");

        String normalizedName = normalizeName(cleanedName);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        boolean customExists =
                exerciseDefinitionRepository
                        .existsByOwnerUsernameAndNormalizedNameAndMuscleGroup(
                                username,
                                normalizedName,
                                request.muscleGroup()
                        );

        boolean systemExists =
                exerciseDefinitionRepository
                        .existsByExerciseTypeAndNormalizedNameAndMuscleGroup(
                                ExerciseType.SYSTEM,
                                normalizedName,
                                request.muscleGroup()
                        );

        if (customExists || systemExists) {
            throw new NameAlreadyExistsException(
                    "Exercise name already exists for this muscle group"
            );
        }

        ExerciseDefinition exerciseDefinition =
                new ExerciseDefinition();

        exerciseDefinition.setName(cleanedName);
        exerciseDefinition.setMuscleGroup(request.muscleGroup());
        exerciseDefinition.setNormalizedName(normalizedName);
        exerciseDefinition.setExerciseType(ExerciseType.CUSTOM);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setOwner(owner);

        ExerciseDefinition savedExerciseDefinition =
                exerciseDefinitionRepository.save(exerciseDefinition);

        exerciseDefinitionCacheService.evictList(username);

        return exerciseDefinitionMapper.toResponse(
                savedExerciseDefinition
        );
    }

    @Transactional(readOnly = true)
    public List<ExerciseDefinitionResponse> findAllExerciseDefinitions() {
        String username = currentUserProvider.getCurrentUsername();

        return exerciseDefinitionCacheService.findAll(username);
    }

    @Transactional(readOnly = true)
    public ExerciseDefinitionResponse findExerciseDefinitionById(
            Long id
    ) {
        String username = currentUserProvider.getCurrentUsername();

        return exerciseDefinitionCacheService.findById(
                username,
                id
        );
    }

    @Transactional
    public ExerciseDefinitionResponse updateExerciseDefinition(
            Long id,
            ExerciseDefinitionRequest request
    ) {
        String username = currentUserProvider.getCurrentUsername();

        ExerciseDefinition exerciseDefinition =
                exerciseDefinitionRepository
                        .findByIdAndOwnerUsernameAndExerciseTypeAndArchivedFalse(
                                id,
                                username,
                                ExerciseType.CUSTOM
                        )
                        .orElseThrow(() ->
                                new ExerciseDefinitionNotFoundException(
                                        "Exercise definition not found"
                                )
                        );

        String cleanedName = request.exerciseName()
                .strip()
                .replaceAll("\\s+", " ");

        String normalizedName = normalizeName(cleanedName);

        boolean customExists =
                exerciseDefinitionRepository
                        .existsByOwnerUsernameAndNormalizedNameAndMuscleGroupAndIdNot(
                                username,
                                normalizedName,
                                request.muscleGroup(),
                                id
                        );

        boolean systemExists =
                exerciseDefinitionRepository
                        .existsByExerciseTypeAndNormalizedNameAndMuscleGroup(
                                ExerciseType.SYSTEM,
                                normalizedName,
                                request.muscleGroup()
                        );

        if (customExists || systemExists) {
            throw new NameAlreadyExistsException(
                    "Exercise name already exists for this muscle group"
            );
        }

        exerciseDefinition.setName(cleanedName);
        exerciseDefinition.setNormalizedName(normalizedName);
        exerciseDefinition.setMuscleGroup(request.muscleGroup());

        exerciseDefinitionCacheService
                .evictListAndExercise(username, id);

        return exerciseDefinitionMapper.toResponse(
                exerciseDefinition
        );
    }

    @Transactional
    public void archiveExerciseDefinition(Long id) {
        String username = currentUserProvider.getCurrentUsername();

        ExerciseDefinition exerciseDefinition =
                exerciseDefinitionRepository
                        .findByIdAndOwnerUsernameAndExerciseTypeAndArchivedFalse(
                                id,
                                username,
                                ExerciseType.CUSTOM
                        )
                        .orElseThrow(() ->
                                new ExerciseDefinitionNotFoundException(
                                        "Exercise definition not found"
                                )
                        );

        exerciseDefinition.setArchived(true);

        exerciseDefinitionCacheService
                .evictListAndExercise(username, id);
    }

    private String normalizeName(String name) {
        return name.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}