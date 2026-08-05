package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateExerciseRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateSetRequest;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.exception.DuplicateExerciseDefinitionException;
import com.cosmin.fitness_tracker_api.exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.exception.NameAlreadyExistsException;
import com.cosmin.fitness_tracker_api.exception.UserNotFoundException;
import com.cosmin.fitness_tracker_api.exception.WorkoutTemplateNotFoundException;
import com.cosmin.fitness_tracker_api.mapper.WorkoutTemplateMapper;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplate;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplateExercise;
import com.cosmin.fitness_tracker_api.model.WorkoutTemplateSet;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import com.cosmin.fitness_tracker_api.repository.WorkoutTemplateExerciseRepository;
import com.cosmin.fitness_tracker_api.repository.WorkoutTemplateRepository;
import com.cosmin.fitness_tracker_api.repository.WorkoutTemplateSetRepository;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class WorkoutTemplateService {

    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;
    private final WorkoutTemplateSetRepository workoutTemplateSetRepository;
    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WorkoutTemplateMapper workoutTemplateMapper;

    public WorkoutTemplateService(
            WorkoutTemplateRepository workoutTemplateRepository,
            WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository,
            WorkoutTemplateSetRepository workoutTemplateSetRepository,
            ExerciseDefinitionRepository exerciseDefinitionRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            WorkoutTemplateMapper workoutTemplateMapper
    ) {
        this.workoutTemplateRepository = workoutTemplateRepository;
        this.workoutTemplateExerciseRepository =
                workoutTemplateExerciseRepository;
        this.workoutTemplateSetRepository =
                workoutTemplateSetRepository;
        this.exerciseDefinitionRepository =
                exerciseDefinitionRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.workoutTemplateMapper = workoutTemplateMapper;
    }

    @Transactional
    public WorkoutTemplateResponse createWorkoutTemplate(
            WorkoutTemplateRequest request
    ) {
        String username =
                currentUserProvider.getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                username + " not found"
                        )
                );

        String cleanName = request.workoutTemplateName()
                .strip()
                .replaceAll("\\s+", " ");

        String normalizedName = normalizeName(cleanName);

        boolean templateExists =
                workoutTemplateRepository
                        .findByUserUsernameAndNormalizedName(
                                username,
                                normalizedName
                        )
                        .isPresent();

        if (templateExists) {
            throw new NameAlreadyExistsException(
                    "Workout template already exists"
            );
        }

        WorkoutTemplate workoutTemplate =
                new WorkoutTemplate();

        workoutTemplate.setCreatedAt(LocalDate.now());
        workoutTemplate.setTemplateName(cleanName);
        workoutTemplate.setNormalizedName(normalizedName);
        workoutTemplate.setUser(user);

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(workoutTemplate);

        createWorkoutTemplateExercisesFromRequest(
                savedTemplate,
                request.templateExerciseRequest(),
                username
        );

        return workoutTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional(readOnly = true)
    public WorkoutTemplateResponse getTemplateById(
            Long templateId
    ) {
        String username =
                currentUserProvider.getCurrentUsername();

        WorkoutTemplate template =
                workoutTemplateRepository
                        .findByIdAndUserUsername(
                                templateId,
                                username
                        )
                        .orElseThrow(() ->
                                new WorkoutTemplateNotFoundException(
                                        "Workout template with id "
                                                + templateId
                                                + " not found"
                                )
                        );

        return workoutTemplateMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkoutTemplateResponse> getAllTemplates(
            int page,
            int size
    ) {
        String username =
                currentUserProvider.getCurrentUsername();

        Pageable pageable =
                PageRequest.of(page, size);

        Page<WorkoutTemplateResponse> responses =
                workoutTemplateRepository
                        .findByUserUsername(
                                username,
                                pageable
                        )
                        .map(workoutTemplateMapper::toResponse);

        return PagedResponse.from(responses);
    }

    @Transactional
    public void deleteTemplateById(Long templateId) {
        String username =
                currentUserProvider.getCurrentUsername();

        WorkoutTemplate template =
                workoutTemplateRepository
                        .findByIdAndUserUsername(
                                templateId,
                                username
                        )
                        .orElseThrow(() ->
                                new WorkoutTemplateNotFoundException(
                                        "Workout template with id "
                                                + templateId
                                                + " not found"
                                )
                        );

        workoutTemplateRepository.delete(template);
    }

    @Transactional(readOnly = true)
    public WorkoutRequest prepareWorkoutFromTemplate(
            Long templateId
    ) {
        String username =
                currentUserProvider.getCurrentUsername();

        WorkoutTemplate workoutTemplate =
                workoutTemplateRepository
                        .findByIdAndUserUsername(
                                templateId,
                                username
                        )
                        .orElseThrow(() ->
                                new WorkoutTemplateNotFoundException(
                                        "Workout template with id "
                                                + templateId
                                                + " not found"
                                )
                        );

        return workoutTemplateMapper.toWorkoutRequest(
                workoutTemplate,
                LocalDate.now()
        );
    }

    private void createWorkoutTemplateExercisesFromRequest(
            WorkoutTemplate workoutTemplate,
            List<WorkoutTemplateExerciseRequest> exerciseRequests,
            String username
    ) {
        Set<Long> exerciseDefinitionIds =
                new HashSet<>();

        for (int index = 0;
             index < exerciseRequests.size();
             index++) {

            WorkoutTemplateExerciseRequest exerciseRequest =
                    exerciseRequests.get(index);

            Long exerciseDefinitionId =
                    exerciseRequest.exerciseDefinitionId();

            if (!exerciseDefinitionIds.add(
                    exerciseDefinitionId
            )) {
                throw new DuplicateExerciseDefinitionException(
                        "Exercise definition with id "
                                + exerciseDefinitionId
                                + " appears more than once"
                );
            }

            ExerciseDefinition exerciseDefinition =
                    exerciseDefinitionRepository
                            .findByIdAccessible(
                                    exerciseDefinitionId,
                                    username,
                                    ExerciseType.SYSTEM
                            )
                            .orElseThrow(() ->
                                    new ExerciseDefinitionNotFoundException(
                                            "Exercise definition with id "
                                                    + exerciseDefinitionId
                                                    + " not found"
                                    )
                            );

            WorkoutTemplateExercise templateExercise =
                    new WorkoutTemplateExercise();

            templateExercise.setExerciseDefinition(
                    exerciseDefinition
            );

            templateExercise.setExerciseNumber(
                    index + 1
            );

            workoutTemplate.addTemplateExercise(
                    templateExercise
            );

            WorkoutTemplateExercise savedExercise =
                    workoutTemplateExerciseRepository.save(
                            templateExercise
                    );

            createWorkoutTemplateSetsFromRequest(
                    savedExercise,
                    exerciseRequest.templateSetRequests()
            );
        }
    }

    private void createWorkoutTemplateSetsFromRequest(
            WorkoutTemplateExercise templateExercise,
            List<WorkoutTemplateSetRequest> setRequests
    ) {
        for (int index = 0;
             index < setRequests.size();
             index++) {

            WorkoutTemplateSetRequest setRequest =
                    setRequests.get(index);

            WorkoutTemplateSet templateSet =
                    new WorkoutTemplateSet();

            templateSet.setSetNumber(index + 1);
            templateSet.setTargetWeight(
                    setRequest.targetWeight()
            );
            templateSet.setTargetReps(
                    setRequest.targetReps()
            );
            templateSet.setTargetRir(
                    setRequest.targetRir()
            );

            templateExercise.addTemplateSet(templateSet);

            workoutTemplateSetRepository.save(templateSet);
        }
    }

    private String normalizeName(String name) {
        return name.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}