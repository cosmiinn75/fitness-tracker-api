package com.cosmin.fitness_tracker_api.security;

import com.cosmin.fitness_tracker_api.security.rateLimit.AuthenticatedRateLimitFilter;
import com.cosmin.fitness_tracker_api.security.rateLimit.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JWTFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;
    private final AuthenticatedRateLimitFilter authenticatedRateLimitFilter;

    public SecurityConfig(JWTFilter jwtFilter, RateLimitFilter rateLimitFilter, AuthenticatedRateLimitFilter authenticatedRateLimitFilter) {
        this.jwtFilter = jwtFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.authenticatedRateLimitFilter = authenticatedRateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(config -> config.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/actuator/health")
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        rateLimitFilter,
                        JWTFilter.class
                )
                .addFilterAfter(
                        authenticatedRateLimitFilter,
                        JWTFilter.class
                )
                .build();
    }


    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitFilter filter
    ) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<JWTFilter> jwtFilterRegistration(
            JWTFilter jwtFilter
    ) {
        FilterRegistrationBean<JWTFilter> registration =
                new FilterRegistrationBean<>(jwtFilter);

        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthenticatedRateLimitFilter> authenticatedRateLimitFilterFilterRegistrationBean(
            AuthenticatedRateLimitFilter  authenticatedRateLimitFilter
    ) {
        FilterRegistrationBean<AuthenticatedRateLimitFilter> registration = new FilterRegistrationBean<>(authenticatedRateLimitFilter);
        registration.setEnabled(false);
        return registration;
    }
}
