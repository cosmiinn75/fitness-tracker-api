package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.security.JWTFilter;
import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitAnswer;
import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitFilter;
import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitPolicy;
import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public abstract class BaseControllerTest {

    @MockitoBean
    protected RateLimitFilter rateLimitFilter;

    @MockitoBean
    private JWTFilter jwtFilter;

}