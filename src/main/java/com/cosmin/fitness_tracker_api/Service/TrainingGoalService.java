package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.Exception.ActiveTrainingGoalAlreadyExistsException;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.UserNotAuthException;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.TrainingGoalRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        LocalDate today = LocalDate.now();

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

        long daysLeft = ChronoUnit.DAYS.between(
                today,
                savedGoal.getTargetDate()
        );

        return new TrainingGoalResponse(
                savedGoal.getExerciseDefinition().getName(),
                savedGoal.getTargetWeight(),
                savedGoal.getTargetReps(),
                savedGoal.getTargetDate(),
                daysLeft
        );
    }

    @Transactional
    public void completeGoalsFromWorkout(Workout workout){

        LocalDate workoutDate = workout.getDate();

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
                    trainingGoalRepository.save(trainingGoal);
                }


        }

    }




    private String getCurrentUsername() {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UserNotAuthException("User is not authenticated");
            }

            return authentication.getName();
        }

}
