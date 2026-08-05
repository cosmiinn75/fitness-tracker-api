package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WorkoutTemplateRequest(
        @NotBlank
        String workoutTemplateName,

        @NotEmpty
        List<@Valid WorkoutTemplateExerciseRequest> templateExerciseRequest
) {
}
