package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Model.WorkoutTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate,Long> {
    Optional<WorkoutTemplate> findByUserUsernameAndNormalizedName(String userUsername, String normalizedName);

    Optional<WorkoutTemplate> findByIdAndUserUsername(Long id, String userUsername);

    Page<WorkoutTemplate> findByUserUsername(String userUsername, Pageable pageable);

    String user(User user);
}
