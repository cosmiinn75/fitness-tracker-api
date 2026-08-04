package com.cosmin.fitness_tracker_api.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "reset_token")
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}
