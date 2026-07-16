package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Service.ExerciseDefinitionService;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/exercises")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Exercise Definitions",
        description = "Endpoints for creating, retrieving and updating exercise definitions used in workouts"
)
public class ExerciseDefinitionController {

    private final ExerciseDefinitionService exerciseDefinitionService;

    public ExerciseDefinitionController(
            ExerciseDefinitionService exerciseDefinitionService
    ) {
        this.exerciseDefinitionService = exerciseDefinitionService;
    }

    @Operation(
            summary = "Get all exercise definitions",
            description = "Returns all exercise definitions available to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise definitions returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            )
    })
    @GetMapping
    public List<ExerciseDefinitionResponse> getAllExercises() {
        return exerciseDefinitionService.findAllExerciseDefinitions();
    }

    @Operation(
            summary = "Get an exercise definition",
            description = "Returns a specific exercise definition by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise definition returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid exercise definition ID"
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
    @GetMapping("/{id}")
    public ExerciseDefinitionResponse getExercise(
            @PathVariable @Positive Long id
    ) {
        return exerciseDefinitionService.findExerciseDefinitionById(id);
    }

    @Operation(
            summary = "Create an exercise definition",
            description = "Creates a new exercise definition that can later be added to workouts"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Exercise definition created successfully"
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
                    responseCode = "409",
                    description = "An exercise definition with the same name already exists"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseDefinitionResponse createExercise(
            @Valid @RequestBody ExerciseDefinitionRequest exerciseDefinitionRequest
    ) {
        return exerciseDefinitionService.addExerciseDefinition(
                exerciseDefinitionRequest
        );
    }

    @Operation(
            summary = "Update an exercise definition",
            description = "Replaces the name and muscle group of an existing exercise definition"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Exercise definition updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or exercise definition ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise definition not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "An exercise definition with the same name already exists"
            )
    })
    @PutMapping("/{id}")
    public ExerciseDefinitionResponse updateExercise(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ExerciseDefinitionRequest exerciseDefinitionRequest
    ) {
        return exerciseDefinitionService.updateExerciseDefinition(
                id,
                exerciseDefinitionRequest
        );
    }
}