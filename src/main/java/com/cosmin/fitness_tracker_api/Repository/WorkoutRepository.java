package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    Page<Workout> findByUserUsernameOrderByDateDesc(String userUsername, Pageable pageable);

    Optional<Workout> findByIdAndUserUsername(Long id, String userUsername);



    List<Workout> findByUserUsernameAndDateBetween(String currentUserName, LocalDate aWeekAgo, LocalDate today);
}
