package com.cosmin.fitness_tracker_api.Service;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.NameAlreadyExistsException;
import com.cosmin.fitness_tracker_api.Exception.UserNotAuthException;
import com.cosmin.fitness_tracker_api.Exception.UserNotFoundException;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class ExerciseDefinitionService {

    private final ExerciseDefinitionRepository exerciseDefinitionRepository;

    private final UserRepository userRepository;

    public ExerciseDefinitionService(ExerciseDefinitionRepository exerciseDefinitionRepository, UserRepository userRepository) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExerciseDefinitionResponse addExerciseDefinition(ExerciseDefinitionRequest request) {
        String username = getCurrentUsername();
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

        return toExerciseDefinitionResponse(savedExerciseDefinition);
    }

    @Transactional(readOnly = true)
    public List<ExerciseDefinitionResponse> findAllExerciseDefinitions() {
        return exerciseDefinitionRepository.findAllAccessible(getCurrentUsername(),ExerciseType.SYSTEM)
                .stream()
                .map(this::toExerciseDefinitionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDefinitionResponse findExerciseDefinitionById(Long id) {
        ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository.findByIdAccessible(id,getCurrentUsername(),ExerciseType.SYSTEM)
                .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

        return toExerciseDefinitionResponse(exerciseDefinition);
    }

    @Transactional
    public ExerciseDefinitionResponse updateExerciseDefinition(
            Long id,
            ExerciseDefinitionRequest request
    ) {
        String username = getCurrentUsername();

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

        return toExerciseDefinitionResponse(exerciseDefinition);
    }


    @Transactional
    public void archiveExerciseDefinition(Long id) {
        ExerciseDefinition exerciseDefinition =
                exerciseDefinitionRepository
                        .findByIdAndOwnerUsernameAndExerciseTypeAndArchivedFalse(
                                id,
                                getCurrentUsername(),
                                ExerciseType.CUSTOM
                        )
                        .orElseThrow(() ->
                                new ExerciseDefinitionNotFoundException(
                                        "Exercise definition not found"
                                )
                        );

        exerciseDefinition.setArchived(true);
    }

    private ExerciseDefinitionResponse toExerciseDefinitionResponse(ExerciseDefinition exerciseDefinition) {
        return new ExerciseDefinitionResponse(
                exerciseDefinition.getId(),
                exerciseDefinition.getName(),
                exerciseDefinition.getMuscleGroup(),
                exerciseDefinition.getExerciseType(),
                exerciseDefinition.isArchived()
        );
    }



    private String normalizeName(String name) {
        return name.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthException("User is not authenticated");
        }

        return authentication.getName();
    }
}