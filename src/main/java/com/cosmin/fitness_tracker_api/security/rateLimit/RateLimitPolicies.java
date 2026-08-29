package com.cosmin.fitness_tracker_api.security.rateLimit;

import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitPolicy;

public final class RateLimitPolicies {

    private RateLimitPolicies() {}

    public static final RateLimitPolicy LOGIN =
            new RateLimitPolicy(5, 1.0, 2000L);

    public static final RateLimitPolicy REGISTER =
            new RateLimitPolicy(3, 1.0, 30000L);

    public static final RateLimitPolicy FORGOT_PASSWORD =
            new RateLimitPolicy(3, 1.0, 60000L);

    public static final RateLimitPolicy REFRESH =
            new RateLimitPolicy(10, 1.0, 1000L);

    public static final RateLimitPolicy AUTHENTICATED_API =
            new RateLimitPolicy(
                    100,
                    10.0,
                    1_000L
            );
}