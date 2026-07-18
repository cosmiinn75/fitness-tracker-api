package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.PersonalRecordResponse;
import com.cosmin.fitness_tracker_api.DTO.VolumeProgressResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutVolumeResponse;
import com.cosmin.fitness_tracker_api.Exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.PersonalRecordNotFoundException;
import com.cosmin.fitness_tracker_api.Exception.UserNotAuthException;
import com.cosmin.fitness_tracker_api.Exception.WorkoutNotFoundException;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.Workout;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class ProgressService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final ExerciseDefinitionRepository exerciseDefinitionRepository;


    public ProgressService(WorkoutRepository workoutRepository, ExerciseSetRepository exerciseSetRepository, ExerciseDefinitionRepository exerciseDefinitionRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
    }

    public WorkoutVolumeResponse getWorkoutVolumeById(Long id) {
        String username = getCurrentUsername();


        Workout workout = workoutRepository.findByIdAndUserUsername(id,username)
                .orElseThrow(
                        () -> new WorkoutNotFoundException("Workout with id: " + id + " not found")
                );

        return new  WorkoutVolumeResponse(calculateWorkoutVolume(workout));
    }


    public VolumeProgressResponse getWeeklyVolume(){
        LocalDate today = LocalDate.now();
        LocalDate aWeekAgo = today.minusDays(6);

        List<Workout> weeklyWorkout = workoutRepository
                .findByUserUsernameAndDateBetween(
                        getCurrentUsername()
                        ,aWeekAgo
                        ,today);

        double totalVolume = weeklyWorkout
                .stream()
                .mapToDouble(this::calculateWorkoutVolume)
                .sum();

        return new  VolumeProgressResponse(
                aWeekAgo,
                today,
                totalVolume
        );

    }



    public PersonalRecordResponse getPersonalRecordByExerciseDefinitionId(Long id) {
        String username = getCurrentUsername();

        ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository.findById(id)
                .orElseThrow( () -> new ExerciseDefinitionNotFoundException("Exercise with id: " + id + " not found"));

        List<ExerciseSet> exerciseSets = exerciseSetRepository
                .findByWorkoutExerciseExerciseDefinitionIdAndWorkoutExerciseWorkoutUserUsername(
                        id,
                        username
                );
        ExerciseSet bestSet = exerciseSets.stream()
                .max(
                        Comparator.comparing(ExerciseSet::getWeight)
                                .thenComparing(ExerciseSet::getReps)
                                .thenComparing(ExerciseSet::getRir)
                                .thenComparing(set -> set.getWorkoutExercise()
                                        .getWorkout()
                                        .getDate()
                                )
                )
                .orElseThrow(() -> new PersonalRecordNotFoundException("No sets found for this exercise"));


        return new PersonalRecordResponse(
                exerciseDefinition.getId(),
                exerciseDefinition.getName(),
                bestSet.getWeight(),
                bestSet.getReps(),
                bestSet.getRir(),
                bestSet.getWorkoutExercise().getWorkout().getDate()
        );
    }


    public VolumeProgressResponse getMonthlyVolume(){
        LocalDate today = LocalDate.now();
        LocalDate aMonthAgo = today.withDayOfMonth(1);

        List<Workout> workouts = workoutRepository.findByUserUsernameAndDateBetween(
                getCurrentUsername(),
                aMonthAgo
                ,today
        );

        double totalVolume = workouts.stream()
                .mapToDouble(this::calculateWorkoutVolume)
                .sum();

        return new   VolumeProgressResponse(
                aMonthAgo,
                today,
                totalVolume
        );
    }




    private double calculateWorkoutVolume(Workout workout) {
        double volume = 0;

        for (WorkoutExercise exercise : workout.getWorkoutExercises()) {
            for (ExerciseSet set : exercise.getExerciseSets()) {
                volume += set.getWeight() * set.getReps();
            }
        }

        return volume;
    }


    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthException("User is not authenticated");
        }

        return authentication.getName();
    }
}
