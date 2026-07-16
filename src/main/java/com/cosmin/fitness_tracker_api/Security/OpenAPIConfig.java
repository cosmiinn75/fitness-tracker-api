package com.cosmin.fitness_tracker_api.Security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fitness Tracker API",
                version = "1.0",
                description = "REST API for managing users, workouts, exercises, sets and fitness progress"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer authentication. Enter the JWT token without the Bearer prefix.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
)
public class OpenAPIConfig {
}