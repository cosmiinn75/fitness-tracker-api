package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record PasswordResetRequest(
        @Email
        String email
) {
}
