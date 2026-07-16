package com.cosmin.fitness_tracker_api.Controller;

import com.cosmin.fitness_tracker_api.DTO.PersonalRecordResponse;
import com.cosmin.fitness_tracker_api.DTO.VolumeProgressResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutVolumeResponse;
import com.cosmin.fitness_tracker_api.Service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/progress")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Progress",
        description = "Endpoints for workout volume, weekly and monthly progress, and personal records"
)
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @Operation(
            summary = "Get weekly workout volume",
            description = "Calculates the total workout volume of the authenticated user during the last seven days, including today"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Weekly workout volume calculated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            )
    })
    @GetMapping("/weekly-volume")
    public VolumeProgressResponse getWeeklyVolume() {
        return progressService.getWeeklyVolume();
    }

    @Operation(
            summary = "Get monthly workout volume",
            description = "Calculates the total workout volume of the authenticated user during the last month"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Monthly workout volume calculated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token"
            )
    })
    @GetMapping("/monthly-volume")
    public VolumeProgressResponse getMonthlyVolume() {
        return progressService.getMonthlyVolume();
    }

    @Operation(
            summary = "Get workout volume",
            description = "Calculates the total volume of a specific workout belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Workout volume calculated successfully"
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
    @GetMapping("/workouts/{workoutId}/volume")
    public WorkoutVolumeResponse getWorkoutVolume(
            @PathVariable @Positive Long workoutId
    ) {
        return progressService.getWorkoutVolumeById(workoutId);
    }

    @Operation(
            summary = "Get personal record",
            description = "Returns the personal record of the authenticated user for a specific exercise. The set with the highest weight is selected, and at equal weight the set with the highest repetitions is selected"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Personal record returned successfully"
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
                    description = "Exercise definition or personal record not found"
            )
    })
    @GetMapping("/exercises/{exerciseDefinitionId}/personal-record")
    public PersonalRecordResponse getPersonalRecord(
            @PathVariable @Positive Long exerciseDefinitionId
    ) {
        return progressService.getPersonalRecordById(
                exerciseDefinitionId
        );
    }
}