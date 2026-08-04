package com.cosmin.fitness_tracker_api.service;


import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.exception.*;
import com.cosmin.fitness_tracker_api.model.*;
import com.cosmin.fitness_tracker_api.repository.*;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutService {

    private final ExerciseDefinitionRepository exerciseDefinitionRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final UserRepository userRepository;
    private final TrainingGoalService trainingGoalService;
    private final CurrentUserProvider currentUserProvider;

    public WorkoutService(ExerciseDefinitionRepository exerciseDefinitionRepository, WorkoutRepository workoutRepository, WorkoutExerciseRepository workoutExerciseRepository, ExerciseSetRepository exerciseSetRepository, UserRepository userRepository, TrainingGoalService trainingGoalService, CurrentUserProvider currentUserProvider) {
        this.exerciseDefinitionRepository = exerciseDefinitionRepository;
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.userRepository = userRepository;
        this.trainingGoalService = trainingGoalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public CreateWorkoutResponse createWorkout(WorkoutRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        Workout workout = new Workout();
        workout.setWorkoutName(request.workoutName());
        workout.setDate(request.date());
        workout.setUser(currentUser);

        Workout savedWorkout = workoutRepository.save(workout);

        List<WorkoutExerciseResponse> exerciseResponses = createWorkoutExercisesFromRequest(savedWorkout,request.exerciseRequests(),username);
        int goalsCompleted = trainingGoalService.completeGoalsFromWorkout(savedWorkout);
        return toCreateWorkoutResponse(savedWorkout, exerciseResponses,goalsCompleted);
    }



    @Transactional(readOnly = true)
    public PagedResponse<WorkoutResponse> getAllWorkoutsFiltered(Integer page ,
                                                                 Integer size,
                                                                 String name,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        String currentUsername = currentUserProvider.getCurrentUsername();

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "Start date must be before or equal to end date"
            );
        }

        String normalizedName = name == null || name.isBlank() ? null : name.trim();

        Pageable pageable =  PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"date"));

       Page<WorkoutResponse> responses = workoutRepository.findFilteredWorkouts(currentUsername,normalizedName,startDate,endDate,pageable)
                .map(this::toWorkoutResponse);
        return PagedResponse.from(responses);
    }

    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutById(Long id) {
        String currentUsername = currentUserProvider.getCurrentUsername();
        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));

        return toWorkoutResponse(workout);
    }

    @Transactional
    public void deleteWorkoutById(Long id) {
        String currentUsername = currentUserProvider.getCurrentUsername();

        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));
        workoutRepository.delete(workout);

    }



    @Transactional
    public WorkoutResponse updateWorkoutMetaData(WorkoutMetaDataRequest request,Long id) {
        String currentUsername = currentUserProvider.getCurrentUsername();

        Workout workout = workoutRepository.findByIdAndUserUsername(id,currentUsername).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));


        if(request.workoutName() != null) {
            workout.setWorkoutName(request.workoutName());
        }

        if(request.date() != null) {
            workout.setDate(request.date());
        }


        trainingGoalService.completeGoalsFromWorkout(workout);
        return toWorkoutResponse(workoutRepository.save(workout));

    }

    @Transactional
    public WorkoutResponse replaceWorkout(Long id, WorkoutRequest request) {
        String currentUsername = currentUserProvider.getCurrentUsername();

        Workout workout = workoutRepository
                .findByIdAndUserUsername(id, currentUsername)
                .orElseThrow(() ->
                        new WorkoutNotFoundException("Workout not found")
                );

        workout.setWorkoutName(request.workoutName());
        workout.setDate(request.date());


        workout.getWorkoutExercises().clear();

        workoutRepository.flush();

        List<WorkoutExerciseResponse> exerciseResponses =
                createWorkoutExercisesFromRequest(
                        workout,
                        request.exerciseRequests(),
                        currentUsername
                );


        trainingGoalService.completeGoalsFromWorkout(workout);

        return toWorkoutResponse(workout, exerciseResponses);
    }

        @Transactional
        public WorkoutResponse changeOneSet(UpdateExerciseSetRequest request, Long workoutId, Integer exerciseNumber , Integer setNumber) {
            String currentUsername = currentUserProvider.getCurrentUsername();

            Workout workout = workoutRepository.findByIdAndUserUsername(workoutId,currentUsername)
                    .orElseThrow( () -> new WorkoutNotFoundException("Workout with id: " + workoutId + " not found"));

            WorkoutExercise workoutExercise = getWorkoutExerciseByExerciseNumber(workout,exerciseNumber);


            ExerciseSet exerciseSet = getExerciseSetBySetNumber(workoutExercise,setNumber);

            if (request.weight() != null) {
                exerciseSet.setWeight(request.weight());
            }

            if (request.reps() != null) {
                exerciseSet.setReps(request.reps());
            }

            if (request.rir() != null) {
                exerciseSet.setRir(request.rir());
            }
            trainingGoalService.completeGoalsFromWorkout(workout);

            return toWorkoutResponse(workout);
        }


        @Transactional
        public WorkoutResponse changeWorkoutExercise(
                ChangeWorkoutExerciseRequest request,
                Long workoutId,
                Integer exerciseNumber
        ) {
            String currentUsername = currentUserProvider.getCurrentUsername();

            Workout workout = workoutRepository
                    .findByIdAndUserUsername(workoutId, currentUsername)
                    .orElseThrow(() -> new WorkoutNotFoundException(
                            "Workout with id: " + workoutId + " not found"
                    ));

            WorkoutExercise workoutExercise = getWorkoutExerciseByExerciseNumber(workout, exerciseNumber);

            ExerciseDefinition newExerciseDefinition =
                    exerciseDefinitionRepository
                            .findByIdAccessible(request.exerciseDefinitionId(),currentUsername,ExerciseType.SYSTEM)
                            .orElseThrow(() ->
                                    new ExerciseDefinitionNotFoundException(
                                            "Exercise definition with id: "
                                                    + request.exerciseDefinitionId()
                                                    + " not found"
                                    )
                            );

            workoutExercise.setExerciseDefinition(newExerciseDefinition);

            trainingGoalService.completeGoalsFromWorkout(workout);

            return toWorkoutResponse(workout);
        }


        @Transactional
        public WorkoutResponse deleteExerciseSet(Long workoutId, Integer exerciseNumber, Integer setNumber) {
            String currentUsername = currentUserProvider.getCurrentUsername();

            Workout workout = workoutRepository.findByIdAndUserUsername(workoutId,currentUsername)
                    .orElseThrow(() -> new WorkoutNotFoundException("Workout with id: " + workoutId + " not found"));

            WorkoutExercise workoutExercise = getWorkoutExerciseByExerciseNumber(workout, exerciseNumber);

           ExerciseSet exerciseSet = getExerciseSetBySetNumber(workoutExercise,setNumber);

           List<ExerciseSet> exerciseSets = workoutExercise.getExerciseSets();




            exerciseSets.remove(exerciseSet);

           for(ExerciseSet set: exerciseSets) {

               if(set.getSetNumber() > setNumber) {
                   set.setSetNumber(set.getSetNumber() - 1);
               }

           }

            exerciseSetRepository.delete(exerciseSet);

            return toWorkoutResponse(workout);
        }




        @Transactional
        public WorkoutResponse addSet(SetRequest request, Long workoutId, Integer exerciseNumber) {

            String currentUsername = currentUserProvider.getCurrentUsername();

            Workout workout = workoutRepository.findByIdAndUserUsername(workoutId,currentUsername)
                    .orElseThrow(() -> new WorkoutNotFoundException("Workout with id: " + workoutId + " not found"));

            WorkoutExercise workoutExercise = getWorkoutExerciseByExerciseNumber(workout, exerciseNumber);

            List<ExerciseSet> exerciseSets = workoutExercise.getExerciseSets();

            ExerciseSet newSet = new ExerciseSet();
            newSet.setWeight(request.weight());
            newSet.setReps(request.reps());
            newSet.setRir(request.rir());
            newSet.setWorkoutExercise(workoutExercise);
            newSet.setSetNumber(exerciseSets.size()+1);

            exerciseSets.add(newSet);
            exerciseSetRepository.save(newSet);

            trainingGoalService.completeGoalsFromWorkout(workout);
            return toWorkoutResponse(workout);
        }



        @Transactional
        public WorkoutResponse deleteWorkoutExercise(Long workoutId, Integer exerciseNumber) {
            String currentUsername = currentUserProvider.getCurrentUsername();

            Workout workout = workoutRepository.findByIdAndUserUsername(workoutId,currentUsername)
                    .orElseThrow(() -> new WorkoutNotFoundException("Workout with id: " + workoutId + " not found"));


            List<WorkoutExercise> workoutExercises = workout.getWorkoutExercises();
            WorkoutExercise workoutExercise = getWorkoutExerciseByExerciseNumber(workout, exerciseNumber);

            workoutExercises.remove(workoutExercise);

            for(WorkoutExercise exercise: workoutExercises) {
                if(exercise.getExerciseNumber() > exerciseNumber ) {
                    exercise.setExerciseNumber(exercise.getExerciseNumber() - 1);
                }
            }

            workoutExerciseRepository.delete(workoutExercise);

            return toWorkoutResponse(workout);
        }


    @Transactional
    public WorkoutResponse addWorkoutExercise(
            Long workoutId,
            WorkoutExerciseRequest exerciseRequest
    ) {
        String currentUsername = currentUserProvider.getCurrentUsername();

        Workout workout = workoutRepository
                .findByIdAndUserUsername(workoutId, currentUsername)
                .orElseThrow(() -> new WorkoutNotFoundException(
                        "Workout with id: " + workoutId + " not found"
                ));

        ExerciseDefinition exerciseDefinition =
                exerciseDefinitionRepository
                        .findByIdAccessible(exerciseRequest.exerciseDefinitionId(),currentUsername,ExerciseType.SYSTEM)
                        .orElseThrow(() ->
                                new ExerciseDefinitionNotFoundException(
                                        "Exercise definition with id: "
                                                + exerciseRequest.exerciseDefinitionId()
                                                + " not found"
                                )
                        );

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkout(workout);
        workoutExercise.setExerciseDefinition(exerciseDefinition);
        workoutExercise.setExerciseNumber(
                workout.getWorkoutExercises().size() + 1
        );
        workoutExercise.setExerciseSets(new ArrayList<>());


        workout.getWorkoutExercises().add(workoutExercise);

        workoutExerciseRepository.save(workoutExercise);

        List<SetRequest> setRequests = exerciseRequest.setRequests();

        for (int i = 0; i < setRequests.size(); i++) {
            SetRequest setRequest = setRequests.get(i);

            ExerciseSet exerciseSet = new ExerciseSet();
            exerciseSet.setWeight(setRequest.weight());
            exerciseSet.setReps(setRequest.reps());
            exerciseSet.setRir(setRequest.rir());
            exerciseSet.setSetNumber(i + 1);
            exerciseSet.setWorkoutExercise(workoutExercise);

            workoutExercise.getExerciseSets().add(exerciseSet);
        }

        exerciseSetRepository.saveAll(
                workoutExercise.getExerciseSets()
        );

        trainingGoalService.completeGoalsFromWorkout(workout);
        return toWorkoutResponse(workout);
    }

    @Transactional
    public WorkoutResponse duplicateWorkout(DuplicateWorkoutRequest request , Long workoutId){

        String username = currentUserProvider.getCurrentUsername();

        Workout workoutToDuplicate = workoutRepository.findByIdAndUserUsername(workoutId,username)
                .orElseThrow(() -> new WorkoutNotFoundException("Workout with id: "+ workoutId + " not found"));

        Workout newWorkout = new Workout();
        newWorkout.setWorkoutName(request.workoutName());
        newWorkout.setDate(request.date());
        newWorkout.setUser(workoutToDuplicate.getUser());

        List<WorkoutExercise> workoutExercises = workoutToDuplicate.getWorkoutExercises();
        List<WorkoutExercise> newWorkoutExercises = new ArrayList<>();
        for (WorkoutExercise workoutExercise : workoutExercises) {

            ExerciseDefinition accessibleExerciseDefinition =
                    exerciseDefinitionRepository.findByIdAccessible(workoutExercise.getExerciseDefinition().getId(),
                                    username,
                                    ExerciseType.SYSTEM)
                            .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Workout contains an archived or inaccessible exercise definition"));

            WorkoutExercise newWorkoutExercise = getWorkoutExercise(workoutExercise, newWorkout);
            newWorkoutExercise.setExerciseDefinition(accessibleExerciseDefinition);
            newWorkoutExercises.add(newWorkoutExercise);

        }

        newWorkout.setWorkoutExercises(newWorkoutExercises);

        workoutRepository.save(newWorkout);

        trainingGoalService.completeGoalsFromWorkout(newWorkout);
        return toWorkoutResponse(newWorkout);
    }

        private WorkoutExercise getWorkoutExerciseByExerciseNumber(Workout workout, Integer exerciseNumber) {

            return workout.getWorkoutExercises()
                    .stream()
                    .filter(exercise ->
                            exercise.getExerciseNumber().equals(exerciseNumber)
                    )
                    .findFirst()
                    .orElseThrow(() -> new WorkoutExerciseNotFoundException(
                            "Exercise with number: " + exerciseNumber + " not found"
                    ));

        }


    private ExerciseSet getExerciseSetBySetNumber(
            WorkoutExercise workoutExercise,
            Integer setNumber
    ) {
        return workoutExercise.getExerciseSets()
                .stream()
                .filter(set -> set.getSetNumber().equals(setNumber))
                .findFirst()
                .orElseThrow(() -> new ExerciseSetNotFoundException(
                        "Set with number: " + setNumber + " not found"
                ));
    }




    private WorkoutResponse toWorkoutResponse(Workout workout){

        List<WorkoutExercise> workoutExercises = workoutExerciseRepository.findByWorkoutOrderByExerciseNumberAsc(workout);
        List<WorkoutExerciseResponse> exerciseResponses = new ArrayList<>();
        for(WorkoutExercise workoutExercise : workoutExercises){

            List<SetResponse> exerciseSets = exerciseSetRepository.findByWorkoutExerciseOrderBySetNumberAsc(workoutExercise)
                    .stream().map(
                            this::toSetResponse
                    ).toList();

            exerciseResponses.add(toExerciseResponse(workoutExercise, exerciseSets));
        }

        return  toWorkoutResponse(workout,exerciseResponses);
    }







    private CreateWorkoutResponse toCreateWorkoutResponse(Workout workout,
                                              List<WorkoutExerciseResponse> exerciseResponses,int goalsCompleted) {
        return new CreateWorkoutResponse(
                workout.getId(),
                workout.getWorkoutName(),
                workout.getDate(),
                exerciseResponses,
                goalsCompleted
        );
    }

    private WorkoutResponse toWorkoutResponse(Workout workout,
                                              List<WorkoutExerciseResponse> exerciseResponses) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getWorkoutName(),
                workout.getDate(),
                exerciseResponses
        );
    }

    private WorkoutExerciseResponse toExerciseResponse(WorkoutExercise exercise, List<SetResponse> setResponses){
        return new WorkoutExerciseResponse(
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
    private List<WorkoutExerciseResponse> createWorkoutExercisesFromRequest(Workout workout , List<WorkoutExerciseRequest> exerciseRequests,String username) {

        List<WorkoutExerciseResponse> exerciseResponses = new ArrayList<>();

        for(int i = 0 ; i < exerciseRequests.size() ; i++) {

            WorkoutExerciseRequest exerciseRequest = exerciseRequests.get(i);



            ExerciseDefinition exerciseDefinition =
                    exerciseDefinitionRepository.findByIdAccessible(exerciseRequest.exerciseDefinitionId(),username, ExerciseType.SYSTEM)
                            .orElseThrow(() -> new ExerciseDefinitionNotFoundException("Exercise definition not found"));

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExerciseNumber(i+1);
            workoutExercise.setExerciseDefinition(exerciseDefinition);
            WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);

            workout.getWorkoutExercises().add(savedWorkoutExercise);

            List<SetRequest>  setRequests = exerciseRequest.setRequests();

            List<SetResponse> setResponses = createExerciseSetsFromRequest(savedWorkoutExercise, setRequests);

            exerciseResponses.add(toExerciseResponse(savedWorkoutExercise,setResponses));

        }

        return exerciseResponses;
    }

    private List<SetResponse> createExerciseSetsFromRequest(WorkoutExercise workoutExercise , List<SetRequest> setRequests){
        List<SetResponse> setResponses = new ArrayList<>();
        for(int i = 0 ; i < setRequests.size() ; i++) {
            SetRequest setRequest = setRequests.get(i);

            ExerciseSet exerciseSet = new ExerciseSet();
            exerciseSet.setWorkoutExercise(workoutExercise);
            exerciseSet.setSetNumber(i+1);
            exerciseSet.setReps(setRequest.reps());
            exerciseSet.setRir(setRequest.rir());
            exerciseSet.setWeight(setRequest.weight());
            ExerciseSet savedSet = exerciseSetRepository.save(exerciseSet);

            workoutExercise.getExerciseSets().add(savedSet);

            setResponses.add(toSetResponse(savedSet));

        }

        return setResponses;
    }


    private static @NonNull WorkoutExercise getWorkoutExercise(WorkoutExercise workoutExercise, Workout newWorkout) {
        WorkoutExercise newWorkoutExercise = new WorkoutExercise();
        newWorkoutExercise.setExerciseNumber(workoutExercise.getExerciseNumber());
        newWorkoutExercise.setWorkout(newWorkout);
        newWorkoutExercise.setExerciseDefinition(workoutExercise.getExerciseDefinition());


        List<ExerciseSet> exerciseSets = workoutExercise.getExerciseSets();
        List<ExerciseSet> newExerciseSets = new ArrayList<>();
        for (ExerciseSet exerciseSet : exerciseSets) {

            ExerciseSet newExerciseSet = new ExerciseSet();
            newExerciseSet.setWeight(exerciseSet.getWeight());
            newExerciseSet.setReps(exerciseSet.getReps());
            newExerciseSet.setRir(exerciseSet.getRir());
            newExerciseSet.setSetNumber(exerciseSet.getSetNumber());
            newExerciseSet.setWorkoutExercise(newWorkoutExercise);

            newExerciseSets.add(newExerciseSet);
        }
        newWorkoutExercise.setExerciseSets(newExerciseSets);
        return newWorkoutExercise;
    }




}
