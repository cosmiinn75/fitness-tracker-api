package com.cosmin.fitness_tracker_api.IntegrationTest;

import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Model.*;
import com.cosmin.fitness_tracker_api.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private ExerciseSetRepository exerciseSetRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {

        exerciseSetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutRepository.deleteAll();
        exerciseDefinitionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "cosmin")
    void createWorkout_ShouldPersistWorkoutWithExerciseAndSets() throws Exception {

        User user = new User();
        user.setUsername("cosmin");
        user.setPassword(passwordEncoder.encode("password1234"));
        user.setEmail("cosmin@gmail.com");

        userRepository.saveAndFlush(user);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        ExerciseDefinition savedExerciseDefinition =
                exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);

        mockMvc.perform(
                        post("/api/workouts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "workoutName": "Push",
                                      "date": "2026-07-05",
                                      "exerciseRequests": [
                                        {
                                          "exerciseDefinitionId": %d,
                                          "setRequests": [
                                            {
                                              "weight": 100.00,
                                              "reps": 5,
                                              "rir": 2
                                            }
                                          ]
                                        }
                                      ]
                                    }
                                    """.formatted(savedExerciseDefinition.getId()))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workoutName").value("Push"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray());



        Workout savedWorkout = workoutRepository.findAll().getFirst();

        assertEquals("Push", savedWorkout.getWorkoutName());

        assertEquals(LocalDate.of(2026, 7, 5), savedWorkout.getDate());
    }



    @Test
    @WithMockUser(username = "cosmin")
    void getWorkoutById_ShouldReturnPersistedWorkout() throws Exception {

        User user = new User();
        user.setUsername("cosmin");
        user.setPassword(passwordEncoder.encode("password1234"));
        user.setEmail("cosmin@gmail.com");

        User savedUser = userRepository.saveAndFlush(user);

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        ExerciseDefinition savedExerciseDefinition =
                exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);

        Workout workout = new Workout();
        workout.setUser(savedUser);
        workout.setWorkoutName("Push");
        workout.setDate(LocalDate.of(2026, 7, 5));

        Workout savedWorkout = workoutRepository.saveAndFlush(workout);

        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setExerciseDefinition(savedExerciseDefinition);
        workoutExercise.setWorkout(savedWorkout);
        workoutExercise.setExerciseNumber(1);

        WorkoutExercise savedWorkoutExercise =
                workoutExerciseRepository.saveAndFlush(workoutExercise);

        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setSetNumber(1);
        exerciseSet.setWeight(100.0);
        exerciseSet.setReps(10);
        exerciseSet.setRir(1);
        exerciseSet.setWorkoutExercise(savedWorkoutExercise);

        exerciseSetRepository.saveAndFlush(exerciseSet);

        mockMvc.perform(
                        get("/api/workouts/{workoutId}", savedWorkout.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName").value("Push"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray())
                .andExpect(jsonPath("$.exerciseResponses.length()").value(1));
    }

    @Test
    @WithMockUser(username = "cosmin")
    void updateWorkoutMetadata_ShouldPersistChanges() throws Exception {

        User user = createUser(
                "cosmin",
                "cosmin@gmail.com"
        );

        Workout savedWorkout = createWorkout(
                user,
                "Push",
                LocalDate.of(2026, 7, 5)
        );

        mockMvc.perform(
                        patch(
                                "/api/workouts/{workoutId}",
                                savedWorkout.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "workoutName": "Pull",
                                      "date": "2026-07-10"
                                    }
                                    """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-10"))
                .andExpect(jsonPath("$.exerciseResponses").isArray());

        Workout updatedWorkout = workoutRepository
                .findById(savedWorkout.getId())
                .orElseThrow();

        assertEquals(
                "Pull",
                updatedWorkout.getWorkoutName()
        );

        assertEquals(
                LocalDate.of(2026, 7, 10),
                updatedWorkout.getDate()
        );
    }

    @Test
    @WithMockUser(username = "cosmin")
    void deleteWorkout_ShouldRemoveWorkoutFromDatabase() throws Exception {

        User user = createUser(
                "cosmin",
                "cosmin@gmail.com"
        );

        Workout savedWorkout = createWorkout(
                user,
                "Push",
                LocalDate.of(2026, 7, 5)
        );

        mockMvc.perform(
                        delete(
                                "/api/workouts/{workoutId}",
                                savedWorkout.getId()
                        )
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        assertFalse(
                workoutRepository.existsById(savedWorkout.getId())
        );
    }


    @Test
    void getWorkoutWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        User user = createUser(
                "cosmin",
                "cosmin@gmail.com"
        );

        Workout savedWorkout = createWorkout(
                user,
                "Push",
                LocalDate.of(2026, 7, 5)
        );

        mockMvc.perform(
                        get(
                                "/api/workouts/{workoutId}",
                                savedWorkout.getId()
                        )
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    private Workout createWorkout(User user, String workoutName, LocalDate date) {

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setWorkoutName(workoutName);
        workout.setDate(date);

        return workoutRepository.saveAndFlush(workout);
    }


    @Test
    @WithMockUser(username = "altUser")
    void getWorkoutOwnedByAnotherUser_ShouldReturnNotFound() throws Exception {

        User owner = createUser(
                "cosmin",
                "cosmin@gmail.com"
        );

        createUser(
                "altUser",
                "altuser@gmail.com"
        );

        Workout savedWorkout = createWorkout(
                owner,
                "Push",
                LocalDate.of(2026, 7, 5)
        );

        mockMvc.perform(
                        get(
                                "/api/workouts/{workoutId}",
                                savedWorkout.getId()
                        )
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private User createUser(String username, String email) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password1234"));

        return userRepository.saveAndFlush(user);
    }

}
