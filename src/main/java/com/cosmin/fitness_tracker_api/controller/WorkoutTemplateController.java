package com.cosmin.fitness_tracker_api.controller;

import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateRequest;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateResponse;
import com.cosmin.fitness_tracker_api.service.WorkoutTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/workout-templates")
@Tag(
        name = "Workout Templates",
        description = "Endpoints for creating, retrieving, deleting and using workout templates"
)
@SecurityRequirement(name = "bearerAuth")
public class WorkoutTemplateController {

    private final WorkoutTemplateService workoutTemplateService;

    public WorkoutTemplateController(
            WorkoutTemplateService workoutTemplateService
    ) {
        this.workoutTemplateService = workoutTemplateService;
    }

    @Operation(
            summary = "Create a workout template",
            description = """
                    Creates a reusable workout template for the authenticated user.
                    Exercise and set numbers are generated according to their order
                    in the request.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Workout template created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or duplicated exercise"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise definition not found or not accessible"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A template with the same name already exists"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutTemplateResponse createWorkoutTemplate(
            @Valid @RequestBody WorkoutTemplateRequest request
    ) {
        return workoutTemplateService.createWorkoutTemplate(request);
    }

    @Operation(
            summary = "Get a workout template",
            description = """
                    Returns one workout template belonging to the authenticated
                    user, including its ordered exercises and sets.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout template returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid template ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout template not found"
            )
    })
    @GetMapping("/{templateId}")
    public WorkoutTemplateResponse getWorkoutTemplate(
            @PathVariable
            @Positive
            Long templateId
    ) {
        return workoutTemplateService.getTemplateById(templateId);
    }

    @Operation(
            summary = "Get all workout templates",
            description = """
                    Returns a paginated list containing the workout templates
                    belonging to the authenticated user.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout templates returned successfully"
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
    public PagedResponse<WorkoutTemplateResponse> getAllWorkoutTemplates(
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    ) {
        return workoutTemplateService.getAllTemplates(page, size);
    }

    @Operation(
            summary = "Prepare a workout from a template",
            description = """
                    Converts the selected template into a pre-filled WorkoutRequest.
                    Nothing is saved in the database. The returned values can be
                    edited and then submitted to POST /api/workouts.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout draft prepared successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid template ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout template not found"
            )
    })
    @GetMapping("/{templateId}/workout-draft")
    public WorkoutRequest prepareWorkoutFromTemplate(
            @PathVariable
            @Positive
            Long templateId
    ) {
        return workoutTemplateService.prepareWorkoutFromTemplate(templateId);
    }

    @Operation(
            summary = "Delete a workout template",
            description = """
                    Permanently deletes a workout template belonging to the
                    authenticated user, including its exercises and sets.
                    Exercise definitions are not deleted.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Workout template deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid template ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Workout template not found"
            )
    })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteWorkoutTemplate(
            @PathVariable
            @Positive
            Long templateId
    ) {
        workoutTemplateService.deleteTemplateById(templateId);
        return ResponseEntity.noContent().build();
    }
}