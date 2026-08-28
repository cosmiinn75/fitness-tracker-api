package com.cosmin.fitness_tracker_api.listener;

import com.cosmin.fitness_tracker_api.event.PasswordResetRequestedEvent;
import com.cosmin.fitness_tracker_api.event.PasswordChangedEvent;
import com.cosmin.fitness_tracker_api.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class EmailEventListener {

    private final EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handlePasswordResetEmail(PasswordResetRequestedEvent event){
        emailService.sendPasswordResetEmail(event.email(),event.rawToken());
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleResetPasswordConfirmationEmail(PasswordChangedEvent event){
        emailService.sendConfirmationEmail(event.email());
    }
}
