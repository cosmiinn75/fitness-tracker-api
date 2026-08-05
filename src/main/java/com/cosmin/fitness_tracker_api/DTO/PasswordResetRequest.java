package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.Email;

public record PasswordResetRequest(
        @Email
        String email
) {
}
