package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.Exception.*;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.TrainingGoalRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingGoalService {

    private final TrainingGoalRepository trainingGoalRepository;
    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final UserRepository userRepository;

    public TrainingGoalService(TrainingGoalRepository trainingGoalRepository, ExerciseDefinitionRepository exerciseDefinitionRepository, UserRepository userRepository) {
        this.trainingGoalRepository = trainingGoalRepository;
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public TrainingGoalResponse createTrainingGoal(TrainingGoalRequest request) {
        String username = getCurrentUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotAuthException("User not authenticated"));


        ExerciseDefinition exerciseDefinition =
                exerciseDefinitionRepository.findById(request.exerciseDefinitionId())
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

        return new TrainingGoalResponse(
                trainingGoal.getId(),
                savedGoal.getExerciseDefinition().getName(),
                savedGoal.getTargetWeight(),
                savedGoal.getTargetReps(),
                savedGoal.getTargetDate(),
                trainingGoal.getStatus()
        );
    }



    @Transactional
    public PagedResponse<TrainingGoalResponse> getTrainingGoals(int page,int size) {

        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);


        trainingGoalRepository.findByUserUsernameOrderByIdAsc(username)
                .forEach(
                        trainingGoal -> {
                            boolean expired = LocalDate.now().isAfter(trainingGoal.getTargetDate());

                            if(expired && trainingGoal.getStatus() == Status.ACTIVE) {
                                trainingGoal.setStatus(Status.EXPIRED);
                            }
                        }
                );


        Page<TrainingGoalResponse> trainingGoalPage = trainingGoalRepository.findByUserUsernameOrderByIdAsc(username,pageable)
                .map(
                        trainingGoal -> new TrainingGoalResponse(
                                trainingGoal.getId(),
                                trainingGoal.getExerciseDefinition().getName(),
                                trainingGoal.getTargetWeight(),
                                trainingGoal.getTargetReps(),
                                trainingGoal.getTargetDate(),
                                trainingGoal.getStatus()
                        )
                );



        return PagedResponse.from(trainingGoalPage);
    }

    @Transactional
    public TrainingGoalResponse cancelTrainingGoal(Long trainingGoalId) {

        TrainingGoal trainingGoal = trainingGoalRepository.findByUserUsernameAndId(getCurrentUsername(),trainingGoalId)
                .orElseThrow(() -> new TrainingGoalNotFoundException("Training goal with id:" + trainingGoalId + " not found"));


        if(trainingGoal.getStatus() != Status.ACTIVE || LocalDate.now().isAfter(trainingGoal.getTargetDate())) {
            throw new InvalidTrainingGoalStatusException(
                    "Only active, non-expired training goals can be cancelled"
            );
        }

        trainingGoal.setStatus(Status.CANCELLED);
        trainingGoalRepository.save(trainingGoal);

        return new TrainingGoalResponse(
                trainingGoalId,
                trainingGoal.getExerciseDefinition().getName(),
                trainingGoal.getTargetWeight(),
                trainingGoal.getTargetReps(),
                trainingGoal.getTargetDate(),
                trainingGoal.getStatus()
        );
    }



    @Transactional
    public int completeGoalsFromWorkout(Workout workout){

        LocalDate workoutDate = workout.getDate();
        int goalsCompleted = 0;
        for(WorkoutExercise workoutExercise : workout.getWorkoutExercises()){
            ExerciseDefinition exerciseDefinition = workoutExercise.getExerciseDefinition();

            TrainingGoal trainingGoal = trainingGoalRepository.findByUserUsernameAndExerciseDefinitionIdAndStatus(
                    getCurrentUsername(), exerciseDefinition.getId(),Status.ACTIVE)
                    .orElse(null);

            if(trainingGoal == null){
                continue;
            }

                if(trainingGoal.getTargetDate().isBefore(workoutDate)){
                    continue;
                }
                if(workoutDate.isBefore(trainingGoal.getCreatedAt())){
                    continue;
                }

                boolean goalReached = workoutExercise.getExerciseSets()
                        .stream()
                        .anyMatch(set -> set.getWeight() >= trainingGoal.getTargetWeight()
                        && set.getReps() >= trainingGoal.getTargetReps());

                if(goalReached){
                    trainingGoal.setStatus(Status.COMPLETED);
                    goalsCompleted++;
                    trainingGoalRepository.save(trainingGoal);
                }


        }
        return goalsCompleted;
    }




    private String getCurrentUsername() {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UserNotAuthException("User is not authenticated");
            }

            return authentication.getName();
        }

}
