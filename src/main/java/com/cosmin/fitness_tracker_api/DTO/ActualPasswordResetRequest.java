package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualPasswordResetRequest(
        @NotBlank
        String token,
        @NotBlank
        @Size(min = 8 , max = 100)
        String newPassword,

        @NotBlank
        String confirmPassword
) {
}
