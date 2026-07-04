package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
