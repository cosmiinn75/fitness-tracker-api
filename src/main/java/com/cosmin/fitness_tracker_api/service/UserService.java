package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.ChangePasswordRequest;
import com.cosmin.fitness_tracker_api.DTO.UserInfoResponse;
import com.cosmin.fitness_tracker_api.exception.InvalidCredentialsException;
import com.cosmin.fitness_tracker_api.exception.UserNotFoundException;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import com.cosmin.fitness_tracker_api.repository.WorkoutRepository;
import com.cosmin.fitness_tracker_api.security.CurrentUserProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public  class UserService  {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    private final PasswordEncoder passwordEncoder;
    private final WorkoutRepository workoutRepository;


    public UserService(UserRepository userRepository, CurrentUserProvider currentUserProvider, PasswordEncoder passwordEncoder, WorkoutRepository workoutRepository) {
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.passwordEncoder = passwordEncoder;
        this.workoutRepository = workoutRepository;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        User loggedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));



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

    @Transactional(readOnly = true)
    public UserInfoResponse getUsersInfo(){
        String username = currentUserProvider.getCurrentUsername();

        User loggedUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        long totalWorkouts = workoutRepository.countByUserUsername(username);

        return new UserInfoResponse(
                username,
                loggedUser.getEmail(),
                totalWorkouts
        );
    }



}
