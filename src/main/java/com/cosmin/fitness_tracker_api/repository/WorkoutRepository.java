package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.model.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {


    @EntityGraph(attributePaths = {"workoutExercises" , "workoutExercises.exerciseDefinition"})
    @Query(value = """
        SELECT wo
        FROM Workout wo
        WHERE wo.id = :id
        AND wo.user.username = :username
""")
    Optional<Workout> findDetailedByIdAndUserUsername(@Param(value = "id") Long id, @Param(value = "username") String userUsername);


    Optional<Workout> findByIdAndUserUsername(Long id , String userUsername);


    @Query(
            value = """
                SELECT w.id
                FROM Workout w
                WHERE w.user.username = :username
                  AND (
                        :name IS NULL
                        OR LOWER(w.workoutName)
                           LIKE LOWER(CONCAT('%', :name, '%'))
                  )
                  AND (
                        :startDate IS NULL
                        OR w.date >= :startDate
                  )
                  AND (
                        :endDate IS NULL
                        OR w.date <= :endDate
                  )
                ORDER BY w.date DESC, w.id DESC
                """,
            countQuery = """
                SELECT COUNT(w.id)
                FROM Workout w
                WHERE w.user.username = :username
                  AND (
                        :name IS NULL
                        OR LOWER(w.workoutName)
                           LIKE LOWER(CONCAT('%', :name, '%'))
                  )
                  AND (
                        :startDate IS NULL
                        OR w.date >= :startDate
                  )
                  AND (
                        :endDate IS NULL
                        OR w.date <= :endDate
                  )
                """
    )
    Page<Long> findFilteredPageIds(
            @Param("username") String username,
            @Param("name") String name,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


    @EntityGraph(attributePaths = {
            "workoutExercises",
            "workoutExercises.exerciseDefinition"
    })
    @Query("""
        SELECT DISTINCT w
        FROM Workout w
        WHERE w.user.username = :username
          AND w.id IN :ids
        """)
    List<Workout> findDetailedByIdsAndUserUsername(
            @Param("ids") List<Long> ids,
            @Param("username") String username
    );




    List<Workout> findByUserUsernameAndDateBetween(String currentUserName, LocalDate aWeekAgo, LocalDate today);

    long countByUserUsername(String username);

    Optional<Workout> findFirstByUserUsernameOrderByDateDesc(String username);


}
