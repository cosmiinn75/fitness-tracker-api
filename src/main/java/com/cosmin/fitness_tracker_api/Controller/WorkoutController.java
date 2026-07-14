package com.cosmin.fitness_tracker_api.Controller;


import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutMetaDataRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutResponse;
import com.cosmin.fitness_tracker_api.Service.WorkoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}
