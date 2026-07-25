package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Size(min = 8, max = 100)
        @NotBlank
        @NotNull
        String oldPassword,

        @Size(min = 8, max = 100)
        @NotBlank
        @NotNull
        String newPassword,

        @Size(min = 8, max = 100)
        @NotBlank
        @NotNull
        String confirmPassword

) {
}
