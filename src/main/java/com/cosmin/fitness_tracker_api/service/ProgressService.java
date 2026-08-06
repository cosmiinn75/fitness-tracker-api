package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.exception.ExerciseDefinitionNotFoundException;
import com.cosmin.fitness_tracker_api.exception.InvalidDateRangeException;
import com.cosmin.fitness_tracker_api.exception.PersonalRecordNotFoundException;
import com.cosmin.fitness_tracker_api.exception.WorkoutNotFoundException;
import com.cosmin.fitness_tracker_api.mapper.ProgressMapper;
import com.cosmin.fitness_tracker_api.mapper.WorkoutMapper;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.model.ExerciseSet;
import com.cosmin.fitness_tracker_api.model.Workout;
import com.cosmin.fitness_tracker_api.model.WorkoutExercise;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.repository.ExerciseSetRepository;
import com.cosmin.fitness_tracker_api.repository.Projection.PersonalRecordProjection;
import com.cosmin.fitness_tracker_api.repository.WorkoutExerciseRepository;
import com.cosmin.fitness_tracker_api.repository.WorkoutRepository;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final CurrentUserProvider currentUserProvider;
    private final ProgressMapper progressMapper;
    private final WorkoutMapper workoutMapper;


    public ProgressService(WorkoutRepository workoutRepository, ExerciseSetRepository exerciseSetRepository, ExerciseDefinitionRepository exerciseDefinitionRepository, WorkoutExerciseRepository workoutExerciseRepository, CurrentUserProvider currentUserProvider, ProgressMapper progressMapper, WorkoutMapper workoutMapper) {
        this.workoutRepository = workoutRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.currentUserProvider = currentUserProvider;
        this.progressMapper = progressMapper;
        this.workoutMapper = workoutMapper;
    }

    public WorkoutVolumeResponse getWorkoutVolumeById(Long id) {
        String username = currentUserProvider.getCurrentUsername();


        Workout workout = workoutRepository.findDetailedByIdAndUserUsername(id,username)
                .orElseThrow(
                        () -> new WorkoutNotFoundException("Workout with id: " + id + " not found")
                );


        return progressMapper.toWorkoutVolumeResponse(calculateWorkoutVolume(workout));
    }


    public VolumeProgressResponse getWeeklyVolume(){
        LocalDate today = LocalDate.now();
        LocalDate aWeekAgo = today.minusDays(6);
        String username = currentUserProvider.getCurrentUsername();

        List<Workout> weeklyWorkout = workoutRepository
                .findByUserUsernameAndDateBetween(
                        username
                        ,aWeekAgo
                        ,today);

        double totalVolume = weeklyWorkout
                .stream()
                .mapToDouble(this::calculateWorkoutVolume)
                .sum();

      return progressMapper.toVolumeProgressResponse(aWeekAgo,today,totalVolume);

    }


    @Transactional(readOnly = true)
    public PagedResponse<PersonalRecordResponse> getPersonalRecords(Integer page, Integer size){
        String username = currentUserProvider.getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);

        Page<PersonalRecordProjection> personalRecords = exerciseSetRepository.findBestExerciseSets(username,pageable);

        Page<PersonalRecordResponse> responses = personalRecords.map(progressMapper::toPersonalRecordResponse);

       return PagedResponse.from(responses);
    }


    public PersonalRecordResponse getPersonalRecordByExerciseDefinitionId(Long id) {
        String username = currentUserProvider.getCurrentUsername();

        ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository.findByIdAccessible(id,username, ExerciseType.SYSTEM)
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



        return progressMapper.toPersonalRecordResponse(exerciseDefinition,bestSet);
    }


    public VolumeProgressResponse getMonthlyVolume(){
        LocalDate today = LocalDate.now();
        LocalDate aMonthAgo = today.withDayOfMonth(1);
        String username = currentUserProvider.getCurrentUsername();

        List<Workout> workouts = workoutRepository.findByUserUsernameAndDateBetween(
                username,
                aMonthAgo
                ,today
        );

        double totalVolume = workouts.stream()
                .mapToDouble(this::calculateWorkoutVolume)
                .sum();


        return progressMapper.toVolumeProgressResponse(aMonthAgo,today,totalVolume);
    }
    @Transactional(readOnly = true)
    public PagedResponse<WorkoutExerciseHistoryResponse> getWorkoutHistory(Long exerciseDefinitionId, LocalDate startDate, LocalDate endDate, Integer page , Integer pageSize) {
        String username = currentUserProvider.getCurrentUsername();

        Pageable pageable = PageRequest.of(page,pageSize);
        if(startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new InvalidDateRangeException("Start date cannot be after end date");

            }
        }

        exerciseDefinitionRepository.findByIdAccessible(exerciseDefinitionId,username,ExerciseType.SYSTEM)
                .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

        Page<WorkoutExerciseHistoryResponse> workoutExerciseHistoryResponses = workoutExerciseRepository.findHistoryByExerciseDefinitionIdAndWorkoutDate(
                        exerciseDefinitionId,username, startDate, endDate,pageable
                )
                .map(
                        workoutExercise -> {
                            List<SetResponse> setResponses = workoutExercise.getExerciseSets()
                                    .stream()
                                    .map(workoutMapper::toSetResponse)
                                    .toList();
                            double estimatedOneRepMax = calculateEstimatedOneRepMax(workoutExercise);

                            return  progressMapper.toWorkoutExerciseHistoryResponse(workoutExercise,setResponses,estimatedOneRepMax);
                        }
                );


        return PagedResponse.from(workoutExerciseHistoryResponses);

    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary() {
        String username = currentUserProvider.getCurrentUsername();

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


        return progressMapper.toSummaryResponse(
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

    private double calculateEstimatedOneRepMax(
            WorkoutExercise workoutExercise
    ) {
        double estimatedOneRepMax = 0.0;

        for (ExerciseSet exerciseSet
                : workoutExercise.getExerciseSets()) {

            double oneRepMax =
                    exerciseSet.getWeight()
                            * (1 + exerciseSet.getReps() / 30.0);

            oneRepMax =
                    Math.round(oneRepMax * 100.0) / 100.0;

            if (oneRepMax > estimatedOneRepMax) {
                estimatedOneRepMax = oneRepMax;
            }
        }

        return estimatedOneRepMax;
    }

}
