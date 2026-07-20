package com.cosmin.fitness_tracker_api.Repository;

import com.cosmin.fitness_tracker_api.Model.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    Page<Workout> findByUserUsernameOrderByDateDesc(String userUsername, Pageable pageable);

    Optional<Workout> findByIdAndUserUsername(Long id, String userUsername);



    @Query("""
        SELECT w
        FROM Workout w
        WHERE w.user.username = :username
            AND (:name IS NULL OR LOWER(w.workoutName) LIKE LOWER(CONCAT('%',:name,'%')) )
            AND (:startDate IS NULL OR w.date >= :startDate)
            AND (:endDate IS NULL OR w.date <= :endDate)
""")

    Page<Workout> findFilteredWorkouts(
            @Param("username") String username,
            @Param("name") String name,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    List<Workout> findByUserUsernameAndDateBetween(String currentUserName, LocalDate aWeekAgo, LocalDate today);
}
