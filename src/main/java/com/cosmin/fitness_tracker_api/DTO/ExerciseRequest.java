package com.cosmin.fitness_tracker_api.DTO;

import java.util.List;

public record ExerciseRequest(
        Long exerciseDefinitionId,
        List<SetRequest> setRequests
) {
}
