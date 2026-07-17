package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.AuthRequest;
import com.cosmin.fitness_tracker_api.DTO.AuthResponse;
import com.cosmin.fitness_tracker_api.DTO.LoginRequest;
import com.cosmin.fitness_tracker_api.DTO.RefreshTokenRequest;
import com.cosmin.fitness_tracker_api.Exception.AccountAlreadyExistsException;
import com.cosmin.fitness_tracker_api.Exception.InvalidCredentialsException;
import com.cosmin.fitness_tracker_api.Model.RefreshToken;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import com.cosmin.fitness_tracker_api.Security.JWTUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JWTUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse register(AuthRequest authRequest) {

        if (userRepository.existsByUsername(authRequest.username())
                || userRepository.existsByEmail(authRequest.email())) {

            throw new AccountAlreadyExistsException(
                    "Username or email already exists"
            );
        }

        User user = new User();
        user.setUsername(authRequest.username());
        user.setEmail(authRequest.email());
        user.setPassword(
                passwordEncoder.encode(authRequest.password())
        );

        User savedUser = userRepository.save(user);

        String accessToken =
                jwtUtil.generateToken(savedUser.getUsername());

        String refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }

    public AuthResponse login(LoginRequest authRequest) {

        User user = userRepository
                .findByUsername(authRequest.username())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid credentials"
                        )
                );

        if (!passwordEncoder.matches(
                authRequest.password(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid credentials"
            );
        }

        String accessToken =
                jwtUtil.generateToken(user.getUsername());

        String refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public AuthResponse refresh(
            RefreshTokenRequest refreshTokenRequest
    ) {

        RefreshToken oldToken =
                refreshTokenService.validateRefreshToken(
                        refreshTokenRequest.refreshToken()
                );

        User user = oldToken.getUser();

        refreshTokenService.revokeRefreshToken(oldToken);

        String newAccessToken =
                jwtUtil.generateToken(user.getUsername());

        String newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.refreshToken());
    }
}
