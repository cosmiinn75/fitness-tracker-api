package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Exception.*;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class WorkoutTemplateService {

    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;
    private final WorkoutTemplateSetRepository workoutTemplateSetRepository;
    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final UserRepository userRepository;


    public WorkoutTemplateService(WorkoutTemplateRepository workoutTemplateRepository, WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository, WorkoutTemplateSetRepository workoutTemplateSetRepository, ExerciseDefinitionRepository exerciseDefinitionRepository, UserRepository userRepository) {
        this.workoutTemplateRepository = workoutTemplateRepository;
        this.workoutTemplateExerciseRepository = workoutTemplateExerciseRepository;
        this.workoutTemplateSetRepository = workoutTemplateSetRepository;
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutTemplateResponse createWorkoutTemplate(WorkoutTemplateRequest request){
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow( () -> new UserNotFoundException(username + " not found"));

        String cleanName = request.workoutTemplateName()
                .strip()
                .replaceAll("\\s+", " ");

        String normalizedName = normalizeName(cleanName);

        WorkoutTemplate template = workoutTemplateRepository.findByUserUsernameAndNormalizedName(username,normalizedName)
                .orElse(null);

        if(template != null){
            throw new NameAlreadyExistsException("Workout template already exists");
        }

        WorkoutTemplate workoutTemplate = new WorkoutTemplate();
        workoutTemplate.setCreatedAt(LocalDate.now());
        workoutTemplate.setTemplateName(cleanName);
        workoutTemplate.setUser(user);
        workoutTemplate.setNormalizedName(normalizedName);

        WorkoutTemplate savedWorkout = workoutTemplateRepository.save(workoutTemplate);

        List<WorkoutTemplateExerciseResponse> exerciseResponses =
                createWorkoutTemplateExercisesFromRequest(
                        savedWorkout,
                        request.templateExerciseRequest(),
                        username
                );

        return toWorkoutTemplateResponse(savedWorkout, exerciseResponses);
    }

    @Transactional(readOnly = true)
    public WorkoutTemplateResponse getTemplateById(Long templateId){
        String username = getCurrentUsername();

        WorkoutTemplate template = workoutTemplateRepository.findByIdAndUserUsername(templateId,username)
                .orElseThrow(() -> new WorkoutTemplateNotFoundException("Workout template with id "+ templateId + " not found"));


        return toWorkoutTemplateResponse(template);
    }


    @Transactional(readOnly = true)
    public PagedResponse<WorkoutTemplateResponse> getAllTemplates(int page, int size){

        String username = getCurrentUsername();

        Pageable pageable = PageRequest.of(page, size);

        Page<WorkoutTemplateResponse> workoutTemplates = workoutTemplateRepository.
                findByUserUsername(username,pageable)
                .map(this::toWorkoutTemplateResponse);

        return PagedResponse.from(workoutTemplates);

    }

    @Transactional
    public void deleteTemplateById(Long templateId){

        WorkoutTemplate template = workoutTemplateRepository.findByIdAndUserUsername(templateId,getCurrentUsername())
                .orElseThrow(() -> new WorkoutTemplateNotFoundException("Workout template with id "+ templateId + " not found"));

        workoutTemplateRepository.delete(template);
    }

    @Transactional(readOnly = true)
    public WorkoutRequest prepareWorkoutFromTemplate(Long templateId){
        String username = getCurrentUsername();

        WorkoutTemplate workoutTemplate = workoutTemplateRepository
                .findByIdAndUserUsername(templateId,username)
                .orElseThrow( () -> new WorkoutTemplateNotFoundException("Workout template with id " + templateId + " not found"));

        List<WorkoutTemplateExercise> templateExercises = workoutTemplate.getTemplateExercises();

        List<WorkoutExerciseRequest> exerciseRequests = templateExercises
                .stream()
                .map(exercise -> {

                    List<SetRequest> setRequests = exercise.getTemplateSets()
                            .stream()
                            .map(set -> {
                                return new SetRequest(
                                        set.getTargetWeight(),
                                        set.getTargetReps(),
                                        set.getTargetRir()
                                );
                            })
                            .toList();
                    return new WorkoutExerciseRequest(
                            exercise.getExerciseDefinition().getId(),
                            setRequests
                    );
                }).toList();

        return new WorkoutRequest(
            workoutTemplate.getTemplateName(),
            LocalDate.now(),
            exerciseRequests
        );

    }


    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthException("User is not authenticated");
        }

        return authentication.getName();
    }


    private WorkoutTemplateResponse toWorkoutTemplateResponse(WorkoutTemplate workoutTemplate){

        List<WorkoutTemplateExercise> workoutExercises =workoutTemplateExerciseRepository.findByWorkoutTemplateOrderByExerciseNumberAsc(workoutTemplate);

        List<WorkoutTemplateExerciseResponse> exerciseResponses = new ArrayList<>();
        for(WorkoutTemplateExercise workoutExercise : workoutExercises){

            List<WorkoutTemplateSetResponse> exerciseSets = workoutTemplateSetRepository.findByWorkoutTemplateExerciseOrderBySetNumberAsc(workoutExercise)
                    .stream().map(
                            this::toWorkoutTemplateSetResponse
                    ).toList();

            exerciseResponses.add(toWorkoutTemplateExerciseResponse(workoutExercise, exerciseSets));
        }

        return  toWorkoutTemplateResponse(workoutTemplate,exerciseResponses);
    }

    private List<WorkoutTemplateExerciseResponse>
    createWorkoutTemplateExercisesFromRequest(
            WorkoutTemplate workoutTemplate,
            List<WorkoutTemplateExerciseRequest> exerciseRequests,
            String username
    ) {
        List<WorkoutTemplateExerciseResponse> exerciseResponses =
                new ArrayList<>();

        Set<Long> exerciseDefinitionIds = new HashSet<>();

        for (int i = 0; i < exerciseRequests.size(); i++) {
            WorkoutTemplateExerciseRequest exerciseRequest =
                    exerciseRequests.get(i);

            Long exerciseDefinitionId =
                    exerciseRequest.exerciseDefinitionId();

            if (!exerciseDefinitionIds.add(exerciseDefinitionId)) {
                throw new DuplicateExerciseDefinitionException(
                        "Exercise definition with id "
                                + exerciseDefinitionId
                                + " appears more than once"
                );
            }

            ExerciseDefinition exerciseDefinition =
                    exerciseDefinitionRepository.findByIdAccessible(
                            exerciseDefinitionId,
                            username,
                            ExerciseType.SYSTEM
                    ).orElseThrow(() ->
                            new ExerciseDefinitionNotFoundException(
                                    "Exercise definition with id "
                                            + exerciseDefinitionId
                                            + " not found"
                            )
                    );

            WorkoutTemplateExercise templateExercise =
                    new WorkoutTemplateExercise();

            templateExercise.setExerciseDefinition(exerciseDefinition);
            templateExercise.setExerciseNumber(i + 1);

            workoutTemplate.addTemplateExercise(templateExercise);

            WorkoutTemplateExercise savedTemplateExercise =
                    workoutTemplateExerciseRepository.save(templateExercise);

            List<WorkoutTemplateSetResponse> templateSets =
                    createWorkoutTemplateSetsFromRequest(
                            savedTemplateExercise,
                            exerciseRequest.templateSetRequests()
                    );

            exerciseResponses.add(
                    toWorkoutTemplateExerciseResponse(
                            savedTemplateExercise,
                            templateSets
                    )
            );
        }

        return exerciseResponses;
    }

    private List<WorkoutTemplateSetResponse> createWorkoutTemplateSetsFromRequest(WorkoutTemplateExercise workoutExercise , List<WorkoutTemplateSetRequest> setRequests){
        List<WorkoutTemplateSetResponse> setResponses = new ArrayList<>();
        for(int i = 0 ; i < setRequests.size() ; i++) {
            WorkoutTemplateSetRequest setRequest = setRequests.get(i);

            WorkoutTemplateSet exerciseSet = new WorkoutTemplateSet();
            workoutExercise.addTemplateSet(exerciseSet);
            exerciseSet.setSetNumber(i+1);
            exerciseSet.setTargetReps(setRequest.targetReps());
            exerciseSet.setTargetRir(setRequest.targetRir());
            exerciseSet.setTargetWeight(setRequest.targetWeight());
            WorkoutTemplateSet savedSet = workoutTemplateSetRepository.save(exerciseSet);


            setResponses.add(toWorkoutTemplateSetResponse(savedSet));

        }

        return setResponses;
    }

    private WorkoutTemplateResponse toWorkoutTemplateResponse(WorkoutTemplate workoutTemplate,
                                                              List<WorkoutTemplateExerciseResponse> exerciseResponses) {
        return new WorkoutTemplateResponse(
                workoutTemplate.getId(),
                workoutTemplate.getTemplateName(),
                workoutTemplate.getCreatedAt(),
                exerciseResponses
        );
    }

    private WorkoutTemplateExerciseResponse toWorkoutTemplateExerciseResponse(WorkoutTemplateExercise exercise, List<WorkoutTemplateSetResponse> setResponses){
        return new WorkoutTemplateExerciseResponse(
                exercise.getId(),
                exercise.getExerciseDefinition().getId(),
                exercise.getExerciseNumber(),
                exercise.getExerciseDefinition().getMuscleGroup(),
                exercise.getExerciseDefinition().getName(),
                setResponses
        );
    }

    private WorkoutTemplateSetResponse toWorkoutTemplateSetResponse(WorkoutTemplateSet exerciseSet) {
        return new WorkoutTemplateSetResponse(
                exerciseSet.getId(),
                exerciseSet.getSetNumber(),
                exerciseSet.getTargetWeight(),
                exerciseSet.getTargetReps(),
                exerciseSet.getTargetRir()
        );
    }
    private String normalizeName(String name) {
        return name.strip()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
