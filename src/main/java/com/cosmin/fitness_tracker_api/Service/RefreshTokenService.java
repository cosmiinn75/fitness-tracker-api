package com.cosmin.fitness_tracker_api.Service;


import com.cosmin.fitness_tracker_api.Exception.InvalidRefreshTokenException;
import com.cosmin.fitness_tracker_api.Model.RefreshToken;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.RefreshTokenRepository;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_EXPIRATION_DURATION = Duration.ofDays(7);


    private final RefreshTokenRepository refreshTokenRepository;


    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String createRefreshToken(User user) {

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plus(REFRESH_TOKEN_EXPIRATION_DURATION));
        refreshToken.setUser(user);

        refreshTokenRepository.save(refreshToken);

        return token;

    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {

        if(token == null || token.isBlank()){
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow( () -> new InvalidRefreshTokenException("Invalid refresh token"));

        if(refreshToken.isRevoked()){
            throw new InvalidRefreshTokenException("Refresh token is revoked");
        }

        if(refreshToken.isExpired()){
            throw new InvalidRefreshTokenException("Refresh token is expired");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }





}
