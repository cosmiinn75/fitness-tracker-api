package com.cosmin.fitness_tracker_api.security.rateLimit;

import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticatedRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public AuthenticatedRateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            filterChain.doFilter(request,response);
            return;
        }

        String username = authentication.getName();
        String key = "rate-limit:api:user" + username;

        RateLimitAnswer result = rateLimitService.tryConsume(key,RateLimitPolicies.AUTHENTICATED_API);

        if(!result.allowed()){

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain");
            response.getWriter().write("Too many requests. Please try again later");
            return;
        }

        filterChain.doFilter(request,response);

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        return request.getRequestURI().startsWith("/api/auth/")
                || request.getRequestURI().startsWith("/swagger-ui/")
                || request.getRequestURI().startsWith("/v3/api-docs/")
                || request.getRequestURI().equals("/actuator/health");
    }

}
