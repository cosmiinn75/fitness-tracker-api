package com.cosmin.fitness_tracker_api.IntegrationTest;


import com.cosmin.fitness_tracker_api.event.PasswordResetRequestedEvent;
import com.cosmin.fitness_tracker_api.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class EmailEventListenerIntegrationTest {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PlatformTransactionManager platformTransactionManager;

    @MockitoBean
    private EmailService emailService;

    private TransactionTemplate transactionTemplate;


    @Autowired
    public EmailEventListenerIntegrationTest(ApplicationEventPublisher applicationEventPublisher, PlatformTransactionManager platformTransactionManager) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.platformTransactionManager = platformTransactionManager;
    }

    @BeforeEach
    void setup(){
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Test
    void shouldSendEmailAfterTransactionCommit(){
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                "commit@test.com",
                "fake-token"
        );

        transactionTemplate.executeWithoutResult(_ -> applicationEventPublisher.publishEvent(event));

        verify(emailService,timeout(1000)).sendPasswordResetEmail("commit@test.com","fake-token");
    }


    @Test
    void shouldNotSendEmailAfterTransactionRollback(){
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(
                "commit@test.com",
                "fake-token"
        );

        transactionTemplate.executeWithoutResult(status -> {
            applicationEventPublisher.publishEvent(event);
            status.setRollbackOnly();
        });

        verify(emailService,after(500).never()).sendPasswordResetEmail("commit@test.com","fake-token");
    }
}
