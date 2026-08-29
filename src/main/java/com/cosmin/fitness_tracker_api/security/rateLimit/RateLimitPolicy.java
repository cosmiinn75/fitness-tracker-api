package com.cosmin.fitness_tracker_api.security.rateLimit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RateLimitPolicy(

        @Positive
        @NotNull
        int capacity,

        @Positive
        @NotNull
        Double tokensPerRefill,

        @Positive
        @NotNull
        Long refillIntervalsMs
) {
}
