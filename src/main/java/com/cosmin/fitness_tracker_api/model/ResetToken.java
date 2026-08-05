package com.cosmin.fitness_tracker_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "reset_token")
@Getter
@Setter
public class ResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "reset_token",unique = true,nullable = false  )
    private String resetToken;


    @Column(name = "expires_at",nullable = false)
    private LocalDateTime expiresAt =  LocalDateTime.now().plusMinutes(15);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ResetToken() {}

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

}
