package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank
        String username,
        @NotBlank

        String email,
        @NotBlank
        String password
) {
}
