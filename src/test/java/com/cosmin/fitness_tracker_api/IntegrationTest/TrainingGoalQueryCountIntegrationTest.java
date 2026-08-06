package com.cosmin.fitness_tracker_api.IntegrationTest;


import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.model.TrainingGoal;
import com.cosmin.fitness_tracker_api.model.User;
import com.cosmin.fitness_tracker_api.repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.repository.TrainingGoalRepository;
import com.cosmin.fitness_tracker_api.repository.UserRepository;
import com.cosmin.fitness_tracker_api.service.TrainingGoalService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrainingGoalQueryCountIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseDefinitionRepository
            exerciseDefinitionRepository;

    @Autowired
    private TrainingGoalRepository trainingGoalRepository;

    @Autowired
    private TrainingGoalService trainingGoalService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @WithMockUser("cosmin")
    void getTrainingGoals_ShouldNotProduceNPlusOneQueries() {


        User user = createUser();

        createTrainingGoal(user,"Bench Press", "bench press");
        createTrainingGoal(user,"Squat", "squat");
        createTrainingGoal(user,"Deadlift" , "deadlift");
        createTrainingGoal(user,"Overhead Press" , "overhead press");

        entityManager.flush();

        entityManager.clear();

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        Statistics statistic = sessionFactory.getStatistics();


        statistic.setStatisticsEnabled(true);

        statistic.clear();


        PagedResponse<TrainingGoalResponse> response = trainingGoalService.getTrainingGoals(0,3);

        long executedStatements = statistic.getPrepareStatementCount();

        assertEquals(3,response.content().size());
        assertEquals(4L,response.totalElements());

        assertEquals(
                "Bench Press",
                response.content()
                        .getFirst()
                        .exerciseName()
        );

        assertEquals(2L, executedStatements);
    }



    private User createUser() {
        User user = new User();

        user.setUsername("cosmin");
        user.setEmail("cosmin@gmail.com");
        user.setPassword("encoded-password");

        return userRepository.saveAndFlush(user);
    }

    private void createTrainingGoal(
            User user,
            String exerciseName,
            String normalizedName
    ) {
        ExerciseDefinition exerciseDefinition =
                new ExerciseDefinition();

        exerciseDefinition.setName(exerciseName);
        exerciseDefinition.setNormalizedName(
                normalizedName
        );
        exerciseDefinition.setExerciseType(
                ExerciseType.SYSTEM
        );
        exerciseDefinition.setMuscleGroup(
                MuscleGroup.CHEST
        );
        exerciseDefinition.setArchived(false);

        ExerciseDefinition savedExerciseDefinition =
                exerciseDefinitionRepository.save(
                        exerciseDefinition
                );

        TrainingGoal trainingGoal =
                new TrainingGoal();

        trainingGoal.setUser(user);
        trainingGoal.setExerciseDefinition(
                savedExerciseDefinition
        );
        trainingGoal.setTargetWeight(100.0);
        trainingGoal.setTargetReps(5);
        trainingGoal.setCreatedAt(
                LocalDate.now()
        );
        trainingGoal.setTargetDate(
                LocalDate.now().plusMonths(3)
        );
        trainingGoal.setStatus(Status.ACTIVE);

        trainingGoalRepository.save(trainingGoal);
    }
}
