package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Size(min = 3, max = 30)
        @NotBlank
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        String username,
        @NotBlank
        @NotNull
        String password
) {
}
