package com.cosmin.fitness_tracker_api.Controller;


import com.cosmin.fitness_tracker_api.DTO.AuthRequest;
import com.cosmin.fitness_tracker_api.DTO.AuthResponse;
import com.cosmin.fitness_tracker_api.DTO.LoginRequest;
import com.cosmin.fitness_tracker_api.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid AuthRequest authRequest) {
        return authService.register(authRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest authRequest) {
        return authService.login(authRequest);
    }

}
