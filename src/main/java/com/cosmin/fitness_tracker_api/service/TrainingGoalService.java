package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.exception.*;
import com.cosmin.fitness_tracker_api.mapper.TrainingGoalMapper;
import com.cosmin.fitness_tracker_api.model.*;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.repository.TrainingGoalRepository;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrainingGoalService {

    private final TrainingGoalRepository trainingGoalRepository;
    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TrainingGoalMapper trainingGoalMapper;

    public TrainingGoalService(TrainingGoalRepository trainingGoalRepository, ExerciseDefinitionRepository exerciseDefinitionRepository, UserRepository userRepository, CurrentUserProvider currentUserProvider, TrainingGoalMapper trainingGoalMapper) {
        this.trainingGoalRepository = trainingGoalRepository;
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.trainingGoalMapper = trainingGoalMapper;
    }

    @Transactional
    public TrainingGoalResponse createTrainingGoal(TrainingGoalRequest request) {
        String username = currentUserProvider.getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotAuthException("User not authenticated"));


        ExerciseDefinition exerciseDefinition =
                exerciseDefinitionRepository.findByIdAccessible(request.exerciseDefinitionId(),username, ExerciseType.SYSTEM)
                        .orElseThrow(() ->
                                new ExerciseDefinitionNotFoundException(
                                        "Exercise definition with id "
                                                + request.exerciseDefinitionId()
                                                + " not found"
                                )
                        );

        boolean activeGoalExists =
                trainingGoalRepository
                        .existsByUserUsernameAndExerciseDefinitionIdAndStatus(
                                username,
                                request.exerciseDefinitionId(),
                                Status.ACTIVE
                        );

        if (activeGoalExists) {
            throw new ActiveTrainingGoalAlreadyExistsException(
                    "An active training goal already exists for this exercise"
            );
        }

        TrainingGoal trainingGoal = new TrainingGoal();
        trainingGoal.setUser(user);
        trainingGoal.setExerciseDefinition(exerciseDefinition);
        trainingGoal.setCreatedAt(LocalDate.now());
        trainingGoal.setStatus(Status.ACTIVE);
        trainingGoal.setTargetDate(request.targetDate());
        trainingGoal.setTargetReps(request.targetReps());
        trainingGoal.setTargetWeight(request.targetWeight());

        TrainingGoal savedGoal = trainingGoalRepository.save(trainingGoal);

        return trainingGoalMapper.toResponse(savedGoal);
    }



    @Transactional(readOnly = true)
    public PagedResponse<TrainingGoalResponse> getTrainingGoals(int page,int size) {

        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        Page<TrainingGoalResponse> trainingGoalPage = trainingGoalRepository.findPageWithExerciseDefinitionByUserUsername(username,pageable)
                .map(trainingGoalMapper::toResponse);
        return PagedResponse.from(trainingGoalPage);
    }

    @Transactional
    public TrainingGoalResponse cancelTrainingGoal(Long trainingGoalId) {

        String username = currentUserProvider.getCurrentUsername();
        TrainingGoal trainingGoal = trainingGoalRepository.findByUserUsernameAndId(username,trainingGoalId)
                .orElseThrow(() -> new TrainingGoalNotFoundException("Training goal with id:" + trainingGoalId + " not found"));


        if(trainingGoal.getStatus() != Status.ACTIVE || LocalDate.now().isAfter(trainingGoal.getTargetDate())) {
            throw new InvalidTrainingGoalStatusException(
                    "Only active, non-expired training goals can be cancelled"
            );
        }

        trainingGoal.setStatus(Status.CANCELLED);

        return trainingGoalMapper.toResponse(trainingGoal);
    }



    @Transactional
    public int completeGoalsFromWorkout(Workout workout) {
        String username = currentUserProvider.getCurrentUsername();
        LocalDate workoutDate = workout.getDate();

        Set<Long> exerciseDefinitionIds = workout.getWorkoutExercises()
                .stream()
                .map(workoutExercise -> workoutExercise.getExerciseDefinition().getId())
                .collect(Collectors.toSet());

        if (exerciseDefinitionIds.isEmpty()) {
            return 0;
        }

        Map<Long, TrainingGoal> goalsByExerciseDefinitionId =
                trainingGoalRepository
                        .findByUserUsernameAndExerciseDefinitionIdInAndStatus(
                                username,
                                exerciseDefinitionIds,
                                Status.ACTIVE
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                trainingGoal -> trainingGoal.getExerciseDefinition().getId(),
                                Function.identity()
                        ));

        int goalsCompleted = 0;

        for (WorkoutExercise workoutExercise : workout.getWorkoutExercises()) {
            Long exerciseDefinitionId =
                    workoutExercise.getExerciseDefinition().getId();

            TrainingGoal trainingGoal =
                    goalsByExerciseDefinitionId.get(exerciseDefinitionId);

            if (trainingGoal == null) {
                continue;
            }

            if (trainingGoal.getTargetDate().isBefore(workoutDate)) {
                continue;
            }

            if (workoutDate.isBefore(trainingGoal.getCreatedAt())) {
                continue;
            }

            boolean goalReached = workoutExercise.getExerciseSets()
                    .stream()
                    .anyMatch(set ->
                            set.getWeight() >= trainingGoal.getTargetWeight()
                                    && set.getReps() >= trainingGoal.getTargetReps()
                    );

            if (goalReached) {
                trainingGoal.setStatus(Status.COMPLETED);
                goalsCompleted++;
                goalsByExerciseDefinitionId.remove(exerciseDefinitionId);
            }
        }

        return goalsCompleted;
    }





}
