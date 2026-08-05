package com.cosmin.fitness_tracker_api.security;

import com.cosmin.fitness_tracker_api.exception.UserNotAuthException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider{

    @Override
    public String getCurrentUsername(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if(authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
            throw new UserNotAuthException(
                    "User is not authenticated"
            );
        }
        return authentication.getName();
    }

}
