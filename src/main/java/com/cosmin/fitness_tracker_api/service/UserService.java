package com.cosmin.fitness_tracker_api.security;

import com.cosmin.fitness_tracker_api.DTO.ChangePasswordRequest;
import com.cosmin.fitness_tracker_api.exception.InvalidCredentialsException;
import com.cosmin.fitness_tracker_api.exception.UserNotAuthException;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User loggedUser = userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new UserNotAuthException("User not authenticated"));



        if(!request.newPassword().equals(request.confirmPassword())) {
            throw new InvalidCredentialsException("Passwords don't match");
        }



        if(!passwordEncoder.matches(request.oldPassword(), loggedUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid old password");
        }

        if(passwordEncoder.matches(request.newPassword(), loggedUser.getPassword())) {
            throw new InvalidCredentialsException("New password must be different from old password");
        }

        loggedUser.setPassword(passwordEncoder.encode(request.newPassword()));

    }

    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthException("User is not authenticated");
        }

        return authentication.getName();
    }

}
