package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.AuthRequest;
import com.cosmin.fitness_tracker_api.DTO.AuthResponse;
import com.cosmin.fitness_tracker_api.DTO.LoginRequest;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import com.cosmin.fitness_tracker_api.Security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(AuthRequest authRequest){
        if(userRepository.existsByUsername(authRequest.username()) || userRepository.existsByEmail(authRequest.email())){
            throw new RuntimeException("Username or Email already exists");
        }
        User user = new User();
        user.setUsername(authRequest.username());
        user.setEmail(authRequest.email());
        user.setPassword(passwordEncoder.encode(authRequest.password()));
        User savedUser = userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(savedUser.getUsername()));
    }

    public AuthResponse login(LoginRequest authRequest){

        User user = userRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if(!passwordEncoder.matches(authRequest.password(), user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }

        return new  AuthResponse(jwtUtil.generateToken(user.getUsername()));
    }


}
