package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.security.JWTFilter;
import com.cosmin.fitness_tracker_api.security.rateLimit.*;
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

    @MockitoBean
    private AuthenticatedRateLimitFilter authenticatedRateLimitFilter;
}