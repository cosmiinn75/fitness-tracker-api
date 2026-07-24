package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.*;


public record AuthRequest(

        @Size(min = 3, max = 30)
        @NotBlank
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        String username,

        @NotBlank
        @Email
        @NotNull
        String email,

        @Size(min = 8, max = 100)
        @NotBlank
        @NotNull
        String password
) {
}
