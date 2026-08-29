package com.cosmin.fitness_tracker_api.security.rateLimit;

import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitPolicy;

public record RateLimitRule(
        String method,
        String path,
        String keyPrefix,
        RateLimitPolicy policy
) {
}