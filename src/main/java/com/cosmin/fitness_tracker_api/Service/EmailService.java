package com.cosmin.fitness_tracker_api.Service;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String email , String token){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setFrom("no-reply@fitness-tracker.local");
        message.setSubject("Fitness Tracker - Password Reset");

        message.setText("""
                A password reset was requested for your account.

                Use the following token to reset your password:

                %s

                The token expires in 15 minutes.

                If you did not request this, ignore this email.
                """.formatted(token));

        mailSender.send(message);
    }


}
