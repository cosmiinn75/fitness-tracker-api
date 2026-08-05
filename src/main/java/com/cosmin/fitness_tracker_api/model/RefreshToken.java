package com.cosmin.fitness_tracker_api.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refreshTokens")
@Getter
@Setter
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


}
