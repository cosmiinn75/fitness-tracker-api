package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Service.TrainingGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training-goals")
@Tag(
        name = "Training Goals",
        description = "Endpoints for creating and managing user training goals"
)
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TrainingGoalController {

    private final TrainingGoalService trainingGoalService;


    public TrainingGoalController(TrainingGoalService trainingGoalService) {
        this.trainingGoalService = trainingGoalService;
    }


    @Operation(
            summary = "Create a training goal",
            description = """
                    Creates a new training goal for the authenticated user.

                    The target date must be in the future, and the referenced
                    exercise definition must exist.

                    A user cannot have more than one active training goal
                    for the same exercise.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Training goal created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or target date"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Exercise definition not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "An active training goal already exists for this exercise"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingGoalResponse createTrainingGoal(@Valid @RequestBody TrainingGoalRequest trainingGoalRequest) {
        return  trainingGoalService.createTrainingGoal(trainingGoalRequest);
    }
}
