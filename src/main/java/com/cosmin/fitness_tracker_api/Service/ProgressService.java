package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Exception.*;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.ExerciseSet;
import com.cosmin.fitness_tracker_api.Model.Workout;
import com.cosmin.fitness_tracker_api.Model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.Repository.Projection.PersonalRecordProjection;
import com.cosmin.fitness_tracker_api.Repository.WorkoutExerciseRepository;
import com.cosmin.fitness_tracker_api.Repository.WorkoutRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class ProgressService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;


    public ProgressService(WorkoutRepository workoutRepository, ExerciseSetRepository exerciseSetRepository, ExerciseDefinitionRepository exerciseDefinitionRepository, WorkoutExerciseRepository workoutExerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
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


    @Transactional(readOnly = true)
    public PagedResponse<PersonalRecordResponse> getPersonalRecords(Integer page, Integer size){
        String username = getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);

        Page<PersonalRecordProjection> personalRecords = exerciseSetRepository.findBestExerciseSets(username,pageable);

        Page<PersonalRecordResponse> responses = personalRecords.map(projection ->
                new   PersonalRecordResponse(
                        projection.getExerciseDefinitionId(),
                        projection.getExerciseName(),
                        projection.getWeight(),
                        projection.getReps(),
                        projection.getRir(),
                        projection.getWorkoutDate()
                ));

       return PagedResponse.from(responses);
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
    @Transactional(readOnly = true)
    public PagedResponse<WorkoutExerciseHistoryResponse> getWorkoutHistory(Long exerciseDefinitionId, LocalDate startDate, LocalDate endDate, Integer page , Integer pageSize) {
        String username = getCurrentUsername();

        Pageable pageable = PageRequest.of(page,pageSize);
        if(startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new InvalidDateRangeException("Start date cannot be after end date");

            }
        }

        exerciseDefinitionRepository.findById(exerciseDefinitionId)
                .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

        Page<WorkoutExerciseHistoryResponse> workoutExerciseHistoryResponses = workoutExerciseRepository.findHistoryByExerciseDefinitionIdAndWorkoutDate(
                        exerciseDefinitionId,username, startDate, endDate,pageable
                )
                .map(
                        workoutExercise -> {
                            List<SetResponse> setResponses = workoutExercise.getExerciseSets()
                                    .stream()
                                    .map(this::toSetResponse)
                                    .toList();

                            return toExerciseHistoryResponse(workoutExercise, setResponses);

                        }
                );


        return PagedResponse.from(workoutExerciseHistoryResponses);

    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary() {
        String username = getCurrentUsername();

        LocalDate today = LocalDate.now();
        LocalDate aWeekAgo = today.minusDays(6);
        LocalDate aMonthAgo = today.minusDays(29);

        long totalWorkouts =
                workoutRepository.countByUserUsername(username);

        List<Workout> workoutsLast30Days =
                workoutRepository.findByUserUsernameAndDateBetween(
                        username,
                        aMonthAgo,
                        today
                );

        long trainingDaysLast7Days = workoutsLast30Days.stream()
                .map(Workout::getDate)
                .filter(date ->
                        !date.isBefore(aWeekAgo))
                .distinct()
                .count();

        long trainingDaysLast30Days = workoutsLast30Days.stream()
                .map(Workout::getDate)
                .distinct()
                .count();

        long totalSetsLast7Days = workoutsLast30Days.stream()
                .filter(workout ->
                        !workout.getDate().isBefore(aWeekAgo))
                .flatMap(workout ->
                        workout.getWorkoutExercises().stream())
                .mapToLong(workoutExercise ->
                        workoutExercise.getExerciseSets().size())
                .sum();

        Map<String,Long> trainedExercises = workoutsLast30Days
                .stream()
                .flatMap(workout -> workout.getWorkoutExercises().stream())
                .collect(Collectors.groupingBy(
                        workoutExercise -> workoutExercise.getExerciseDefinition()
                                .getName(),
                        Collectors.summingLong(
                                workoutExercise -> workoutExercise.getExerciseSets()
                                        .size()
                        )
                ));
        String mostTrainedExercise = trainedExercises.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        LocalDate lastWorkoutDate =
                workoutRepository
                        .findFirstByUserUsernameOrderByDateDesc(username)
                        .map(Workout::getDate)
                        .orElse(null);

        return new SummaryResponse(
                totalWorkouts,
                trainingDaysLast7Days,
                trainingDaysLast30Days,
                totalSetsLast7Days,
                lastWorkoutDate,
                mostTrainedExercise
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

    private WorkoutExerciseHistoryResponse toExerciseHistoryResponse(
            WorkoutExercise exercise,
            List<SetResponse> setResponses
    ) {
        double estimatedOneRepMax = 0.0;

        for (SetResponse setResponse : setResponses) {
            double oneRepMax = setResponse.weight()
                    * (1 + setResponse.reps() / 30.0);
            oneRepMax = Math.round(oneRepMax * 100.0) / 100.0;
            if (oneRepMax > estimatedOneRepMax) {
                estimatedOneRepMax = oneRepMax;
            }
        }

        return new WorkoutExerciseHistoryResponse(
                exercise.getWorkout().getId(),
                exercise.getId(),
                exercise.getExerciseNumber(),
                exercise.getExerciseDefinition().getName(),
                estimatedOneRepMax,
                exercise.getWorkout().getDate(),
                setResponses
        );
    }

    private SetResponse toSetResponse(ExerciseSet exerciseSet) {
        return new SetResponse(
                exerciseSet.getId(),
                exerciseSet.getSetNumber(),
                exerciseSet.getWeight(),
                exerciseSet.getReps(),
                exerciseSet.getRir()
        );
    }



}
