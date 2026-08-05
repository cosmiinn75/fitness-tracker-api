package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.model.ResetToken;
import com.cosmin.fitness_tracker_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    void deleteAllByUser(User user);

    Optional<ResetToken> findByResetToken(String resetToken);
}
