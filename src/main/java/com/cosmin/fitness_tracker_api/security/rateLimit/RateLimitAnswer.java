package com.cosmin.fitness_tracker_api.security.rateLimit;

public record RateLimitAnswer(
        boolean allowed,
        Double remainingTokens
) {
}
