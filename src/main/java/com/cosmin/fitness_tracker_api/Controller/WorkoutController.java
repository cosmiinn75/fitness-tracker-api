package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;



@Validated
@RestController
@RequestMapping("/api/workouts")
@Tag(
        name = "Workouts",
        description = "Endpoints for creating, retrieving, updating and deleting workouts, exercises and sets"
)
@SecurityRequirement(name = "bearerAuth")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @Operation(
            summary = "Get all workouts",
            description = "Returns a paginated list containing the workouts, exercises and sets of the authenticated user.Apply filters such as workout name , start date and end date."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workouts returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            )
    })
    @GetMapping
    public PagedResponse<WorkoutResponse> getAllWorkouts(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "10")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            String name,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return workoutService.getAllWorkoutsFiltered(
                page,
                size,
                name,
                startDate,
                endDate
        );
    }

    @Operation(
            summary = "Get a workout",
            description = "Returns a specific workout belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid workout ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout not found"
            )
    })
    @GetMapping("/{id}")
    public WorkoutResponse getWorkoutById(
            @PathVariable @Positive Long id
    ) {
        return workoutService.getWorkoutById(id);
    }

    @Operation(
            summary = "Delete a workout",
            description = "Deletes a specific workout belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid workout ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout not found"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkoutById(
            @PathVariable @Positive Long id
    ) {
        workoutService.deleteWorkoutById(id);
    }

    @Operation(
            summary = "Update workout metadata",
            description = "Updates the name or date of a workout belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout metadata updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or workout ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout not found"
            )
    })
    @PatchMapping("/{id}")
    public WorkoutResponse updateWorkoutMetadata(
            @PathVariable @Positive Long id,
            @Valid @RequestBody WorkoutMetaDataRequest request
    ) {
        return workoutService.updateWorkoutMetaData(request, id);
    }

    @Operation(
            summary = "Replace a workout",
            description = "Replaces the exercises and sets of an existing workout belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout replaced successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or workout ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout or exercise definition not found"
            )
    })
    @PutMapping("/{id}")
    public WorkoutResponse updateWorkout(
            @PathVariable @Positive Long id,
            @Valid @RequestBody WorkoutRequest request
    ) {
        return workoutService.replaceWorkout(id, request);
    }

    @Operation(
            summary = "Create a workout",
            description = "Creates a new workout with exercises and sets for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Workout created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise definition not found"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateWorkoutResponse createWorkout(
            @Valid @RequestBody WorkoutRequest workoutRequest
    ) {
        return workoutService.createWorkout(workoutRequest);
    }

    @Operation(
            summary = "Update a set",
            description = "Updates only the provided fields of a specific set. Weight, repetitions and RIR are optional"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Set updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or path parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout, exercise or set not found"
            )
    })
    @PatchMapping("/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}")
    public WorkoutResponse changeOneSet(
            @PathVariable @Positive Long workoutId,
            @PathVariable @Positive Integer exerciseNumber,
            @PathVariable @Positive Integer setNumber,
            @Valid @RequestBody UpdateExerciseSetRequest request
    ) {
        return workoutService.changeOneSet(
                request,
                workoutId,
                exerciseNumber,
                setNumber
        );
    }

    @Operation(
            summary = "Change an exercise",
            description = "Replaces the exercise definition of a workout exercise while keeping its sets unchanged"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or path parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout, workout exercise or exercise definition not found"
            )
    })
    @PatchMapping("/{workoutId}/exercises/{exerciseNumber}")
    public WorkoutResponse changeWorkoutExercise(
            @PathVariable @Positive Long workoutId,
            @PathVariable @Positive Integer exerciseNumber,
            @Valid @RequestBody ChangeWorkoutExerciseRequest request
    ) {
        return workoutService.changeWorkoutExercise(
                request,
                workoutId,
                exerciseNumber
        );
    }

    @Operation(
            summary = "Add a set",
            description = "Adds a new set at the end of a specific workout exercise"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Set added successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or path parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout or workout exercise not found"
            )
    })
    @PostMapping("/{workoutId}/exercises/{exerciseNumber}/sets")
    public WorkoutResponse addSet(
            @Valid @RequestBody SetRequest request,
            @PathVariable @Positive Long workoutId,
            @PathVariable @Positive Integer exerciseNumber
    ) {
        return workoutService.addSet(
                request,
                workoutId,
                exerciseNumber
        );
    }

    @Operation(
            summary = "Delete a set",
            description = "Deletes a specific set and renumbers the remaining sets"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Set deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid path parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout, workout exercise or set not found"
            )
    })
    @DeleteMapping("/{workoutId}/exercises/{exerciseNumber}/sets/{setNumber}")
    @ResponseStatus(HttpStatus.OK)
    public WorkoutResponse deleteSet(
            @PathVariable @Positive Long workoutId,
            @PathVariable @Positive Integer exerciseNumber,
            @PathVariable @Positive Integer setNumber
    ) {
        return workoutService.deleteExerciseSet(
                workoutId,
                exerciseNumber,
                setNumber
        );
    }

    @Operation(
            summary = "Add an exercise",
            description = "Adds a new exercise with its sets at the end of an existing workout"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise added successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or workout ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout or exercise definition not found"
            )
    })
    @PostMapping("/{workoutId}/exercises")
    public WorkoutResponse addWorkoutExercise(
            @PathVariable @Positive Long workoutId,
            @Valid @RequestBody WorkoutExerciseRequest request
    ) {
        return workoutService.addWorkoutExercise(
                workoutId,
                request
        );
    }

    @Operation(
            summary = "Delete an exercise",
            description = "Deletes a workout exercise with all its sets and renumbers the remaining exercises"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid path parameters"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout or workout exercise not found"
            )
    })
    @DeleteMapping("/{workoutId}/exercises/{exerciseNumber}")
    public WorkoutResponse deleteWorkoutExercise(
            @PathVariable @Positive Long workoutId,
            @PathVariable @Positive Integer exerciseNumber
    ) {
        return workoutService.deleteWorkoutExercise(
                workoutId,
                exerciseNumber
        );
    }


    @Operation(
            summary = "Duplicate a workout",
            description = "Creates a new workout by copying all exercises and sets from an existing workout"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout duplicated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid workout ID or request body"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout not found"
            )
    })
    @PostMapping("/{workoutId}/duplicate")
    public WorkoutResponse duplicateWorkout(
            @Valid @RequestBody DuplicateWorkoutRequest request,
            @PathVariable @Positive Long workoutId
    )
    {
        return workoutService.duplicateWorkout(request,workoutId);
    }
}