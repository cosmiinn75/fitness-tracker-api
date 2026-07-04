package com.cosmin.fitness_tracker_api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SetResponse(
        Long id,
        Integer setNumber,
        Double weight,
        Integer reps,
        Integer rir
) {
}
