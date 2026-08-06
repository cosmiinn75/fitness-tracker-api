package com.cosmin.fitness_tracker_api.repository;

import com.cosmin.fitness_tracker_api.model.WorkoutTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate,Long> {
    Optional<WorkoutTemplate> findByUserUsernameAndNormalizedName(String userUsername, String normalizedName);


    @EntityGraph(attributePaths = {"templateExercises", "templateExercises.exerciseDefinition"})

    @Query(value = """
    SELECT wt
    FROM WorkoutTemplate wt
    WHERE wt.id = :id
    AND wt.user.username = :username
""")
    Optional<WorkoutTemplate> findDetailedByIdAndUserUsername(@Param(value = "id") Long id,@Param(value = "username") String userUsername);

    Optional<WorkoutTemplate> findByIdAndUserUsername(Long id , String username);



    @Query(value = """
        SELECT wt.id
        FROM WorkoutTemplate wt
        WHERE wt.user.username = :username
        ORDER BY wt.id ASC
""" ,
    countQuery = """
        SELECT COUNT(wt)
        FROM WorkoutTemplate wt
        WHERE wt.user.username = :username
""")
    Page<Long> findPageIdsByUsername(@Param(value = "username") String username , Pageable pageable);


    @EntityGraph(attributePaths = {
            "templateExercises", "templateExercises.exerciseDefinition"
    })
    @Query(value = """
        SELECT DISTINCT wt
        FROM WorkoutTemplate wt
        WHERE wt.id IN :ids
        AND wt.user.username = :username
""")
    List<WorkoutTemplate> findDetailedByIdsAndUserUsername(@Param(value = "ids") List<Long> ids ,@Param(value = "username") String userUsername);

}
