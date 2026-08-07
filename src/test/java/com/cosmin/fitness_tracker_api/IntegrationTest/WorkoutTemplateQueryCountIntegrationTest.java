package com.cosmin.fitness_tracker_api.IntegrationTest;

import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutTemplateResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.model.*;
import com.cosmin.fitness_tracker_api.repository.*;
import com.cosmin.fitness_tracker_api.service.WorkoutTemplateService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkoutTemplateQueryCountIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private  WorkoutTemplateRepository workoutTemplateRepository;

    @Autowired
    private  WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;

    @Autowired
    private  WorkoutTemplateSetRepository workoutTemplateSetRepository;

    @Autowired
    private  ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private WorkoutTemplateService workoutTemplateService;

    @Test
    @WithMockUser(username = "cosmin")
    void getWorkoutTemplateById_ShouldNotProduceNPlusOneQueries() {

        User user = createUser();

        ExerciseDefinition exerciseDefinition1 =
                createExerciseDefinition(
                        "Bench",
                        "bench"
                );

        ExerciseDefinition exerciseDefinition2 =
                createExerciseDefinition(
                        "Deadlift",
                        "deadlift"
                );

        ExerciseDefinition exerciseDefinition3 =
                createExerciseDefinition(
                        "Squat",
                        "squat"
                );

        WorkoutTemplate workoutTemplate =
                createWorkoutTemplate(
                        "Template",
                        "template",
                        user
                );

        WorkoutTemplateExercise exercise1 =
                createWorkoutTemplateExercise(
                        workoutTemplate,
                        1,
                        exerciseDefinition1
                );

        WorkoutTemplateExercise exercise2 =
                createWorkoutTemplateExercise(
                        workoutTemplate,
                        2,
                        exerciseDefinition2
                );

        WorkoutTemplateExercise exercise3 =
                createWorkoutTemplateExercise(
                        workoutTemplate,
                        3,
                        exerciseDefinition3
                );

        createTemplateSet(
                exercise1,
                1,
                100.0,
                5,
                0
        );

        createTemplateSet(
                exercise1,
                2,
                100.0,
                5,
                0
        );

        createTemplateSet(
                exercise2,
                1,
                100.0,
                5,
                0
        );

        createTemplateSet(
                exercise2,
                2,
                100.0,
                5,
                0
        );

        createTemplateSet(
                exercise3,
                1,
                100.0,
                5,
                0
        );

        createTemplateSet(
                exercise3,
                2,
                100.0,
                5,
                0
        );

        entityManager.flush();
        entityManager.clear();

        SessionFactory sessionFactory =
                entityManagerFactory.unwrap(
                        SessionFactory.class
                );

        Statistics statistics =
                sessionFactory.getStatistics();

        statistics.setStatisticsEnabled(true);
        statistics.clear();

        WorkoutTemplateResponse response =workoutTemplateService.getTemplateById(workoutTemplate.getId());

        long executedStatements = statistics.getPrepareStatementCount();

        assertEquals(workoutTemplate.getId(),
                response.id()
        );

        assertEquals("Template",
                response.workoutName()
        );

        assertEquals(3,
                response.templateExercises().size()
        );

        assertEquals("Bench",
                response.templateExercises()
                        .get(0)
                        .exerciseName()
        );

        assertEquals("Deadlift",
                response.templateExercises()
                        .get(1)
                        .exerciseName()
        );

        assertEquals("Squat",
                response.templateExercises()
                        .get(2)
                        .exerciseName()
        );

        assertTrue(response.templateExercises()
                        .stream()
                        .allMatch(exercise ->
                                exercise.templateSets().size() == 2
                        )
        );

        assertEquals(
                2L,
                executedStatements
        );
    }

    @Test
    @WithMockUser(username = "cosmin")
    void getAllTemplates_ShouldUseConstantNumberOfQueries() {

        User user = createUser();

        /*
         * Creăm 4 template-uri, iar pagina va conține doar 2.
         * Astfel, Spring execută și query-ul de COUNT.
         */
        for (int templateIndex = 1;
             templateIndex <= 4;
             templateIndex++) {

            WorkoutTemplate workoutTemplate =
                    createWorkoutTemplate(
                            "Template " + templateIndex,
                            "template " + templateIndex,
                            user
                    );

            /*
             * Fiecare template are 3 exerciții diferite.
             */
            for (int exerciseIndex = 1;
                 exerciseIndex <= 3;
                 exerciseIndex++) {

                String exerciseName =
                        "Exercise "
                                + templateIndex
                                + "-"
                                + exerciseIndex;

                String normalizedName =
                        "exercise-"
                                + templateIndex
                                + "-"
                                + exerciseIndex;

                ExerciseDefinition exerciseDefinition =
                        createExerciseDefinition(
                                exerciseName,
                                normalizedName
                        );

                WorkoutTemplateExercise templateExercise =
                        createWorkoutTemplateExercise(
                                workoutTemplate,
                                exerciseIndex,
                                exerciseDefinition
                        );

                /*
                 * Fiecare exercițiu are 2 seturi.
                 */
                createTemplateSet(
                        templateExercise,
                        1,
                        100.0,
                        5,
                        1
                );

                createTemplateSet(
                        templateExercise,
                        2,
                        105.0,
                        4,
                        1
                );
            }
        }


        entityManager.flush();

        entityManager.clear();

        SessionFactory sessionFactory =
                entityManagerFactory.unwrap(
                        SessionFactory.class
                );

        Statistics statistics =
                sessionFactory.getStatistics();

        statistics.setStatisticsEnabled(true);
        statistics.clear();


        PagedResponse<WorkoutTemplateResponse> response =
                workoutTemplateService.getAllTemplates(
                        0,
                        2
                );

        long executedStatements =
                statistics.getPrepareStatementCount();


        assertEquals(
                2,
                response.content().size()
        );

        assertEquals(
                4L,
                response.totalElements()
        );

        /*
         * Verificăm că fiecare template din pagină
         * conține toate cele 3 exerciții.
         */
        assertTrue(
                response.content()
                        .stream()
                        .allMatch(template ->
                                template.templateExercises()
                                        .size() == 3
                        )
        );

        /*
         * Verificăm că fiecare exercițiu conține
         * exact cele 2 seturi create.
         */
        assertTrue(
                response.content()
                        .stream()
                        .flatMap(template ->
                                template.templateExercises()
                                        .stream()
                        )
                        .allMatch(exercise ->
                                exercise.templateSets()
                                        .size() == 2
                        )
        );

        /*
         * Query 1: pagina de ID-uri
         * Query 2: COUNT
         * Query 3: templates + exercises + definitions
         * Query 4: toate seturile prin batch fetching
         */
        assertEquals(
                4L,
                executedStatements
        );
    }

    private User createUser(){
        User user = new User();

        user.setUsername("cosmin");
        user.setPassword("encoded-password");
        user.setEmail("cosmin@gmail.com");

        return userRepository.saveAndFlush(user);
    }


    private WorkoutTemplate createWorkoutTemplate(String templateName , String normalizedName , User user ){

        WorkoutTemplate workoutTemplate = new WorkoutTemplate();
        workoutTemplate.setNormalizedName(normalizedName);
        workoutTemplate.setUser(user);
        workoutTemplate.setTemplateName(templateName);
        workoutTemplate.setCreatedAt(LocalDate.now());

        return workoutTemplateRepository.saveAndFlush(workoutTemplate);

    }


    private ExerciseDefinition createExerciseDefinition(String exerciseName , String normalizedName) {
        ExerciseDefinition  exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName(exerciseName);
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setNormalizedName(normalizedName);


        return exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);
    }

    private WorkoutTemplateExercise createWorkoutTemplateExercise(WorkoutTemplate workoutTemplate, Integer exerciseNumber  ,ExerciseDefinition exerciseDefinition ){

        WorkoutTemplateExercise exercise = new WorkoutTemplateExercise();
        exercise.setExerciseDefinition(exerciseDefinition);
        exercise.setWorkoutTemplate(workoutTemplate);
        exercise.setExerciseNumber(exerciseNumber);

        return workoutTemplateExerciseRepository.saveAndFlush(exercise);
    }

    private WorkoutTemplateSet createTemplateSet(WorkoutTemplateExercise exercise,Integer setNumber , Double targetWeight, Integer targetReps, Integer targetRir){

        WorkoutTemplateSet set = new WorkoutTemplateSet();
        set.setSetNumber(setNumber);
        set.setTargetReps(targetReps);
        set.setTargetRir(targetRir);
        set.setTargetWeight(targetWeight);
        set.setWorkoutTemplateExercise(exercise);

        return  workoutTemplateSetRepository.saveAndFlush(set);

    }

}
