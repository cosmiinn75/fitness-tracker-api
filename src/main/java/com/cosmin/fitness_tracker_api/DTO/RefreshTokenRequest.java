package com.cosmin.fitness_tracker_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotBlank
        @NotNull
        String refreshToken
) {
}
