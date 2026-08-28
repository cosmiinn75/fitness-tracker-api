package com.cosmin.fitness_tracker_api.event;

public record PasswordChangedEvent(
        String email
) {
}
