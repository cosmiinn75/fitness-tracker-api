package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.model.RefreshToken;
import com.cosmin.fitness_tracker_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(User user);
}
