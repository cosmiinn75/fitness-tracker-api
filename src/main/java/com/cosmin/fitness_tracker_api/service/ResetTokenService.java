package com.cosmin.fitness_tracker_api.service;

import com.cosmin.fitness_tracker_api.DTO.ActualPasswordResetRequest;
import com.cosmin.fitness_tracker_api.DTO.PasswordResetRequest;
import com.cosmin.fitness_tracker_api.event.PasswordResetRequestedEvent;
import com.cosmin.fitness_tracker_api.event.PasswordChangedEvent;
import com.cosmin.fitness_tracker_api.exception.InvalidCredentialsException;
import com.cosmin.fitness_tracker_api.model.ResetToken;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.repository.ResetTokenRepository;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class ResetTokenService {

    private final ResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder
            = Base64.getUrlEncoder().withoutPadding();

    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public ResetTokenService(ResetTokenRepository resetTokenRepository, ApplicationEventPublisher applicationEventPublisher, UserRepository userRepository,PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.resetTokenRepository = resetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }


    @Transactional
    public void processRequest(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null) {
            return;
        }
        resetTokenRepository.deleteAllByUser(user);

        String rawToken = generateResetToken();
        String tokenHash = hashToken(rawToken);

        ResetToken resetToken = new ResetToken();
        resetToken.setUser(user);
        resetToken.setResetToken(tokenHash);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        resetTokenRepository.save(resetToken);

        applicationEventPublisher.publishEvent(new PasswordResetRequestedEvent(
                user.getEmail(),
                rawToken
        ));

    }

    @Transactional
    public void resetPassword(ActualPasswordResetRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new InvalidCredentialsException("Passwords don't match");
        }

        String tokenHash = hashToken(request.token());
        ResetToken resetToken = resetTokenRepository.findByResetToken(tokenHash)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new InvalidCredentialsException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("New password must be different than the old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        refreshTokenService.revokeAllTokensForUser(user);

        applicationEventPublisher.publishEvent(new PasswordChangedEvent(
                user.getEmail()
        ));

        resetTokenRepository.deleteAllByUser(user);
    }


    private String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = messageDigest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

}
