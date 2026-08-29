package com.cosmin.fitness_tracker_api.security.rateLimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private static final List<RateLimitRule> RULES = List.of(

            new RateLimitRule(
                    "POST",
                    "/api/auth/login",
                    "rate-limit:login:ip:",
                    RateLimitPolicies.LOGIN
            ),

            new RateLimitRule(
                    "POST",
                    "/api/auth/register",
                    "rate-limit:register:ip:",
                    RateLimitPolicies.REGISTER
            ),

            new RateLimitRule(
                    "POST",
                    "/api/auth/forgot-password",
                    "rate-limit:forgot-password:ip:",
                    RateLimitPolicies.FORGOT_PASSWORD
            ),

            new RateLimitRule(
                    "POST",
                    "/api/auth/refresh",
                    "rate-limit:refresh:ip:",
                    RateLimitPolicies.REFRESH
            )
    );

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request , HttpServletResponse response , FilterChain filterChain) throws ServletException, IOException {

       RateLimitRule rule = RULES.stream()
               .filter(r ->
                       r.method().equalsIgnoreCase(request.getMethod())
                       && r.path().equals(request.getRequestURI())
               )
               .findFirst()
               .orElse(null);

        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key =
                rule.keyPrefix() + request.getRemoteAddr();

        RateLimitAnswer result = rateLimitService.tryConsume(key,rule.policy());

        if(!result.allowed()){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain");
            response.getWriter().write("Too many requests. Please try again later");
            return;
        }

        filterChain.doFilter(request,response);


    }
}
