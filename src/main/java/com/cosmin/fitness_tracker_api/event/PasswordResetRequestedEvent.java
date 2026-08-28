package com.cosmin.fitness_tracker_api.event;

public record PasswordResetRequestedEvent(
        String email,
        String rawToken
) {
}
