package com.cosmin.fitness_tracker_api.Controller;


import com.cosmin.fitness_tracker_api.DTO.AuthRequest;
import com.cosmin.fitness_tracker_api.DTO.AuthResponse;
import com.cosmin.fitness_tracker_api.DTO.LoginRequest;
import com.cosmin.fitness_tracker_api.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Endpoints for register and login users"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }



    @Operation(
          summary = "Register a new user",
          description = "Create a new user , encrypts password and returns a token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200" , description = "User registered successfully and JWT token returned"),
            @ApiResponse(responseCode = "400" , description = "Invalid request body"),
            @ApiResponse(responseCode = "409" , description = "Username or email already exists")
    })
    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid AuthRequest authRequest) {
        return authService.register(authRequest);
    }



    @Operation(
            summary = "Login user",
            description = "Authenticates an already existing user with username and password, then returns JWT token"
    )

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200" , description = "User authenticated successfully and JWT token returned"),
            @ApiResponse(responseCode = "400" , description = "Invalid request body"),
            @ApiResponse(responseCode =  "401" , description = "Invalid username or password")
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest authRequest) {
        return authService.login(authRequest);
    }

}
