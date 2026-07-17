package com.cosmin.fitness_tracker_api.Model;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refreshTokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false , length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant revokedAt;

    public RefreshToken() {
    }

    public RefreshToken(Long id, Instant revokedAt, Instant expiresAt, User user, Instant createdAt, String tokenHash) {
        this.id = id;
        this.revokedAt = revokedAt;
        this.expiresAt = expiresAt;
        this.user = user;
        this.createdAt = createdAt;
        this.token = tokenHash;
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public boolean isActive() {
        return !isExpired() && !isRevoked();
    }

    public boolean isRevoked(){
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenHash) {
        this.token = tokenHash;
    }
}
