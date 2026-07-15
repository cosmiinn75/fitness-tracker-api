package com.cosmin.fitness_tracker_api.Controller;


import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Service.WorkoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@Validated
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }


    @GetMapping
    public PagedResponse<WorkoutResponse> getAllWorkouts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return workoutService.getAllWorkouts(page,size);
    }

    @GetMapping("/{id}")
    public WorkoutResponse getWorkoutById(@PathVariable @Positive Long id) {
        return workoutService.getWorkoutById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkoutById(@PathVariable @Positive Long id) {
        workoutService.deleteWorkoutById(id);
    }

    @PatchMapping("/{id}")
    public WorkoutResponse updateWorkoutMetadata(@PathVariable @Positive Long id ,@Valid @RequestBody WorkoutMetaDataRequest request) {
        return workoutService.updateWorkoutMetaData(request, id);
    }

    @PutMapping("/{id}")
    public WorkoutResponse updateWorkout(@PathVariable @Positive Long id ,@Valid @RequestBody WorkoutRequest request) {
        return workoutService.replaceWorkout(id,request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse createWorkout(@RequestBody @Valid WorkoutRequest workoutRequest) {
        return workoutService.createWorkout(workoutRequest);
    }


    @PatchMapping("/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}")
    public WorkoutResponse changeOneSet(
            @PathVariable Long workoutId,
            @PathVariable Integer exerciseNumber,
            @PathVariable Integer setNumber,
            @Valid @RequestBody UpdateExerciseSetRequest request
    ) {
        return workoutService.changeOneSet(
                request,
                workoutId,
                exerciseNumber,
                setNumber
        );


    }
    @PatchMapping("/{workoutId}/exercises/{exerciseNumber}")
    public WorkoutResponse changeWorkoutExercise(
            @PathVariable @Positive Long workoutId,
            @PathVariable @Positive Integer exerciseNumber,
            @Valid  @RequestBody   ChangeWorkoutExerciseRequest request
    ) {
        return workoutService.changeWorkoutExercise(request,workoutId,exerciseNumber);
    }
}
