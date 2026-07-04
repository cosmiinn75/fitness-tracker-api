package com.cosmin.fitness_tracker_api.Service;


import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.*;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;

import org.hibernate.jdbc.Work;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutService {

    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final UserRepository userRepository;


    public WorkoutService(ExerciseDefinitionRepository exerciseDefinitionRepository, WorkoutRepository workoutRepository, WorkoutExerciseRepository workoutExerciseRepository, ExerciseSetRepository exerciseSetRepository, UserRepository userRepository) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponse createWorkout(WorkoutRequest request) {
        User currentUser = userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.exerciseRequests() == null || request.exerciseRequests().isEmpty()) {
            throw new RuntimeException("Workout must have at least one exercise");
        }

        Workout workout = new Workout();
        workout.setWorkoutName(request.workoutName());
        workout.setDate(request.date());
        workout.setUser(currentUser);

        Workout savedWorkout = workoutRepository.save(workout);

        List<ExerciseRequest> exerciseRequests = request.exerciseRequests();
        List<ExerciseResponse> exerciseResponses = new ArrayList<>();

        for (int i = 0; i < exerciseRequests.size(); i++) {
            ExerciseRequest exerciseRequest = exerciseRequests.get(i);

            ExerciseDefinition exerciseDefinition = exerciseDefinitionRepository
                    .findById(exerciseRequest.exerciseDefinitionId())
                    .orElseThrow(() -> new RuntimeException("Exercise definition not found"));

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(savedWorkout);
            workoutExercise.setExerciseNumber(i + 1);
            workoutExercise.setExerciseDefinition(exerciseDefinition);

            WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);

            List<SetRequest> setRequests = exerciseRequest.setRequests();

            if (setRequests == null || setRequests.isEmpty()) {
                throw new RuntimeException("Exercise must have at least one set");
            }

            List<SetResponse> setResponses = new ArrayList<>();

            for (int j = 0; j < setRequests.size(); j++) {
                SetRequest setRequest = setRequests.get(j);

                ExerciseSet exerciseSet = new ExerciseSet();
                exerciseSet.setWorkoutExercise(savedWorkoutExercise);
                exerciseSet.setSetNumber(j + 1);
                exerciseSet.setReps(setRequest.reps());
                exerciseSet.setRir(setRequest.rir());
                exerciseSet.setWeight(setRequest.weight());

                ExerciseSet savedSet = exerciseSetRepository.save(exerciseSet);

                setResponses.add(toSetResponse(savedSet));
            }

            exerciseResponses.add(toExerciseResponse(savedWorkoutExercise,setResponses));
        }

        return toWorkoutResponse(savedWorkout, exerciseResponses);
    }



    @Transactional(readOnly = true)
    public List<WorkoutResponse> getAllWorkouts() {
        String currentUsername = getCurrentUsername();

        return workoutRepository.findByUserUsernameOrderByDateDesc(currentUsername)
                .stream()
                .map(this::toWorkoutResponse)
                .toList();

    }

    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutById(Long id) {
        String currentUsername = getCurrentUsername();
        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new RuntimeException("Workout not found"));

        return toWorkoutResponse(workout);
    }

    @Transactional
    public void deleteWorkoutById(Long id) {
        String currentUsername = getCurrentUsername();

        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new RuntimeException("Workout not found"));
        workoutRepository.delete(workout);

    }



    @Transactional
    public WorkoutResponse updateWorkoutMetaData(WorkoutMetaDataRequest request,Long id) {
        String currentUsername = getCurrentUsername();

        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new RuntimeException("Workout not found"));

        workout.setDate(request.date());
        workout.setWorkoutName(request.workoutName());

        return toWorkoutResponse(workoutRepository.save(workout));

    }

    @Transactional
    public WorkoutResponse replaceWorkout(Long id , WorkoutRequest request) {
        String currentUsername = getCurrentUsername();

        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new RuntimeException("Workout not found"));

        if(request.exerciseRequests() == null || request.exerciseRequests().isEmpty()) {
            throw new  RuntimeException("Exercise requests must have at least one exercise");
        }

        workout.setWorkoutName(request.workoutName());
        workout.setDate(request.date());

        List<WorkoutExercise> oldExercises =
                workoutExerciseRepository.findByWorkoutOrderByExerciseNumberAsc(workout);

        for(WorkoutExercise oldExercise: oldExercises) {
            exerciseSetRepository.deleteByWorkoutExercise(oldExercise);
        }

        workoutExerciseRepository.deleteByWorkout(workout);

        List<ExerciseResponse> exerciseResponses = new ArrayList<>();

        for(int i = 0 ; i < request.exerciseRequests().size(); i++) {
            ExerciseRequest exerciseRequest = request.exerciseRequests().get(i);

            ExerciseDefinition exerciseDefinition =
                    exerciseDefinitionRepository.findById(exerciseRequest.exerciseDefinitionId()).orElseThrow(() -> new RuntimeException("Exercise definition not found"));
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExerciseNumber(i + 1);
            workoutExercise.setExerciseDefinition(exerciseDefinition);

            WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);

            List<SetRequest> setRequests = exerciseRequest.setRequests();

            if(setRequests == null || setRequests.isEmpty()) {
                throw new RuntimeException("Exercise must have at least one set");
            }

            List<SetResponse> setResponses = new ArrayList<>();
            for(int j = 0 ; j < setRequests.size(); j++) {
                SetRequest setRequest = setRequests.get(j);

                ExerciseSet exerciseSet = new ExerciseSet();
                exerciseSet.setWorkoutExercise(savedWorkoutExercise);
                exerciseSet.setSetNumber(j + 1);
                exerciseSet.setReps(setRequest.reps());
                exerciseSet.setRir(setRequest.rir());
                exerciseSet.setWeight(setRequest.weight());

                ExerciseSet savedSet = exerciseSetRepository.save(exerciseSet);
                setResponses.add(toSetResponse(savedSet));
            }

            exerciseResponses.add(toExerciseResponse(savedWorkoutExercise,setResponses));
        }

        return toWorkoutResponse(workout,exerciseResponses);
    }


    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        return authentication.getName();
    }


    private WorkoutResponse toWorkoutResponse(Workout workout){

        List<WorkoutExercise> workoutExercises = workoutExerciseRepository.findByWorkoutOrderByExerciseNumberAsc(workout);
        List<ExerciseResponse> exerciseResponses = new ArrayList<>();
        for(WorkoutExercise workoutExercise : workoutExercises){

            List<SetResponse> exerciseSets = exerciseSetRepository.findByWorkoutExerciseOrderBySetNumberAsc(workoutExercise)
                    .stream().map(
                            this::toSetResponse
                    ).toList();

            exerciseResponses.add(toExerciseResponse(workoutExercise, exerciseSets));
        }

        return  toWorkoutResponse(workout,exerciseResponses);
    }


    private WorkoutResponse toWorkoutResponse(Workout workout,
                                              List<ExerciseResponse> exerciseResponses) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getWorkoutName(),
                workout.getDate(),
                exerciseResponses
        );
    }

    private ExerciseResponse toExerciseResponse(WorkoutExercise exercise,List<SetResponse> setResponses){
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getExerciseNumber(),
                exercise.getExerciseDefinition().getName(),
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
