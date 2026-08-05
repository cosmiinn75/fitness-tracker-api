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

    public ExerciseDefinitionService(ExerciseDefinitionRepository exerciseDefinitionRepository, UserRepository userRepository, CurrentUserProvider currentUserProvider, ExerciseDefinitionMapper exerciseDefinitionMapper) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.exerciseDefinitionMapper = exerciseDefinitionMapper;
    }

    @Transactional
    public ExerciseDefinitionResponse addExerciseDefinition(ExerciseDefinitionRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        String cleanedName = request.exerciseName()
                .strip()
                .replaceAll("\\s+", " ");
        String normalizedName = normalizeName(cleanedName);
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (exerciseDefinitionRepository.existsByOwnerUsernameAndNormalizedName(username, normalizedName)
        || exerciseDefinitionRepository.existsByExerciseTypeAndNormalizedName(ExerciseType.SYSTEM, normalizedName)) {
            throw new NameAlreadyExistsException("Exercise name already exists");
        }



        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName(cleanedName);
        exerciseDefinition.setMuscleGroup(request.muscleGroup());
        exerciseDefinition.setNormalizedName(normalizedName);
        exerciseDefinition.setExerciseType(ExerciseType.CUSTOM);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setOwner(owner);

        ExerciseDefinition savedExerciseDefinition = exerciseDefinitionRepository.save(exerciseDefinition);

        return exerciseDefinitionMapper.toResponse(savedExerciseDefinition);
    }

    @Transactional(readOnly = true)
    public List<ExerciseDefinitionResponse> findAllExerciseDefinitions() {
        String username = currentUserProvider.getCurrentUsername();
        return exerciseDefinitionRepository.findAllAccessible(username,ExerciseType.SYSTEM)
                .stream()
                .map(exerciseDefinitionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDefinitionResponse findExerciseDefinitionById(Long id) {
        String username = currentUserProvider.getCurrentUsername();
        ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository.findByIdAccessible(id,username,ExerciseType.SYSTEM)
                .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

        return exerciseDefinitionMapper.toResponse(exerciseDefinition);
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

        if (!exerciseDefinition.getNormalizedName().equals(normalizedName)
                && (
                exerciseDefinitionRepository
                        .existsByOwnerUsernameAndNormalizedName(
                                username,
                                normalizedName
                        )
                        || exerciseDefinitionRepository
                        .existsByExerciseTypeAndNormalizedName(
                                ExerciseType.SYSTEM,
                                normalizedName
                        )
        )) {
            throw new NameAlreadyExistsException(
                    "Exercise name already exists"
            );
        }

        exerciseDefinition.setName(cleanedName);
        exerciseDefinition.setNormalizedName(normalizedName);
        exerciseDefinition.setMuscleGroup(request.muscleGroup());

        return exerciseDefinitionMapper.toResponse(exerciseDefinition);
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
    }


    private String normalizeName(String name) {
        return name.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }


}