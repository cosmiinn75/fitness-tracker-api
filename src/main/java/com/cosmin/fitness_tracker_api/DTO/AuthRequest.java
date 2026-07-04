package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(

        @Size(min = 3, max = 30)
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        String username,

        @NotBlank
        @Email
        String email,

        @Size(min = 8, max = 100)
        @NotBlank
        String password
) {
}
