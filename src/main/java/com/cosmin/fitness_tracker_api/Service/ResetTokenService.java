package com.cosmin.fitness_tracker_api.Service;

import com.cosmin.fitness_tracker_api.DTO.ActualPasswordResetRequest;
import com.cosmin.fitness_tracker_api.DTO.PasswordResetRequest;
import com.cosmin.fitness_tracker_api.Exception.InvalidCredentialsException;
import com.cosmin.fitness_tracker_api.Model.ResetToken;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.ResetTokenRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class ResetTokenService {

    private final ResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder
            = Base64.getUrlEncoder().withoutPadding();

    private final PasswordEncoder passwordEncoder;

    public ResetTokenService(ResetTokenRepository resetTokenRepository, UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.resetTokenRepository = resetTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
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

        String token = generateResetToken();

        ResetToken resetToken = new ResetToken();
        resetToken.setUser(user);
        resetToken.setResetToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        resetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

    }

    @Transactional
    public void resetPassword(ActualPasswordResetRequest request){
        if(!request.newPassword().equals(request.confirmPassword())){
            throw new InvalidCredentialsException("Passwords don't match");
        }

        ResetToken resetToken = resetTokenRepository.findByResetToken(request.token())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token"));

        if(resetToken.isExpired()){
            throw new InvalidCredentialsException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();

        if(passwordEncoder.matches(request.newPassword(), user.getPassword())){
            throw new InvalidCredentialsException("New password must be different than the old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        resetTokenRepository.deleteAllByUser(user);
    }


}
