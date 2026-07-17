package com.cosmin.fitness_tracker_api.DTO;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
