package com.cosmin.fitness_tracker_api.DTO;

public record UserInfoResponse(
        String username,
        String email,
        long totalWorkouts
) {
}
