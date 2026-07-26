package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.ResetToken;
import com.cosmin.fitness_tracker_api.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    void deleteAllByUser(User user);

    Optional<ResetToken> findByResetToken(String resetToken);
}
