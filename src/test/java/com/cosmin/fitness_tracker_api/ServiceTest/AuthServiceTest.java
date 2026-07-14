package com.cosmin.fitness_tracker_api.ServiceTest;

import com.cosmin.fitness_tracker_api.DTO.AuthRequest;
import com.cosmin.fitness_tracker_api.DTO.AuthResponse;
import com.cosmin.fitness_tracker_api.DTO.LoginRequest;
import com.cosmin.fitness_tracker_api.Exception.AccountAlreadyExistsException;
import com.cosmin.fitness_tracker_api.Exception.InvalidCredentialsException;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import com.cosmin.fitness_tracker_api.Security.JWTUtil;
import com.cosmin.fitness_tracker_api.Service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private AuthService authService;


    @Test
    void register_ShouldCreateUserAndReturnToken(){

        AuthRequest authRequest = new AuthRequest(
         "cosmin",
         "anghel@gmail.com",
         "parola"
        );

        when(userRepository.existsByUsername(authRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(authRequest.email())).thenReturn(false);



        when(passwordEncoder.encode("parola")).thenReturn("encodedPassword");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("cosmin");
        savedUser.setEmail("anghel@gmail.com");
        savedUser.setPassword("encodedPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtil.generateToken("cosmin")).thenReturn("token");
        AuthResponse response = authService.register(authRequest);

        assertEquals("token",response.token());

        verify(userRepository).existsByUsername("cosmin");
        verify(userRepository).existsByEmail("anghel@gmail.com");
        verify(passwordEncoder).encode("parola");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("cosmin");
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        AuthRequest authRequest = new AuthRequest(
                "cosmin",
                "cosmin@gmail.com",
                "parola"
        );

        when(userRepository.existsByUsername("cosmin")).thenReturn(true);

        assertThrows(
                AccountAlreadyExistsException.class,
                () -> authService.register(authRequest)
        );

        verify(userRepository).existsByUsername("cosmin");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        AuthRequest authRequest = new AuthRequest(
                "cosmin",
                "cosmin@gmail.com",
                "parola"
        );

        when(userRepository.existsByUsername("cosmin")).thenReturn(false);
        when(userRepository.existsByEmail("cosmin@gmail.com")).thenReturn(true);

        assertThrows(
                AccountAlreadyExistsException.class,
                () -> authService.register(authRequest)
        );

        verify(userRepository).existsByUsername("cosmin");
        verify(userRepository).existsByEmail("cosmin@gmail.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest loginRequest = new LoginRequest(
                "cosmin",
                "parola"
        );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");
        user.setPassword("encodedPassword");


        when(userRepository.findByUsername("cosmin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("parola", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("cosmin")).thenReturn("token");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("token", response.token());

        verify(userRepository).findByUsername("cosmin");
        verify(passwordEncoder).matches("parola", "encodedPassword");
        verify(jwtUtil).generateToken("cosmin");
    }
    
    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        LoginRequest loginRequest = new LoginRequest(
                "cosmin",
                "parola"
        );

        User user = new User();
        user.setId(1L);
        user.setUsername("cosmin");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername("cosmin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("parola", "encodedPassword")).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );


        verify(userRepository).findByUsername("cosmin");
        verify(passwordEncoder).matches("parola", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString());
    }

}
