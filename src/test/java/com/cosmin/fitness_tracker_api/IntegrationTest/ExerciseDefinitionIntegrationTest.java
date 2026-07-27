package com.cosmin.fitness_tracker_api.IntegrationTest;


import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExerciseDefinitionIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExerciseDefinitionRepository exerciseDefinitionRepository;

    @Autowired
    private UserRepository userRepository;

    private ExerciseDefinition systemExercise;
    private ExerciseDefinition cosminExercise;
    private ExerciseDefinition ionutExercise;

    @BeforeEach
    void setUp() {
        exerciseDefinitionRepository.deleteAll();
        userRepository.deleteAll();
        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);
        exerciseDefinition.setExerciseType(ExerciseType.SYSTEM);
        exerciseDefinition.setArchived(false);
        exerciseDefinition.setNormalizedName("bench press");


        User user1 = new User();
        user1.setUsername("cosmin");
        user1.setEmail("cosmin@gmail.com");
        user1.setPassword("cosmin1234");


        User user2 = new User();
        user2.setUsername("ionut");
        user2.setEmail("ionut@gmail.com");
        user2.setPassword("ionut1234");

        ExerciseDefinition exerciseDefinition1 = new ExerciseDefinition();
        exerciseDefinition1.setName("Lat Pulldown");
        exerciseDefinition1.setMuscleGroup(MuscleGroup.BACK);
        exerciseDefinition1.setOwner(user1);
        exerciseDefinition1.setExerciseType(ExerciseType.CUSTOM);
        exerciseDefinition1.setArchived(false);
        exerciseDefinition1.setNormalizedName("lat pulldown");


        ExerciseDefinition otherExerciseDefinition = new ExerciseDefinition();
        otherExerciseDefinition.setName("Bicep Curls");
        otherExerciseDefinition.setMuscleGroup(MuscleGroup.ARMS);
        otherExerciseDefinition.setOwner(user2);
        otherExerciseDefinition.setExerciseType(ExerciseType.CUSTOM);
        otherExerciseDefinition.setArchived(false);
        otherExerciseDefinition.setNormalizedName("bicep curls");


        userRepository.saveAllAndFlush(List.of(user1,user2));
        systemExercise =
                exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);

        ionutExercise =
                exerciseDefinitionRepository.saveAndFlush(otherExerciseDefinition);

        cosminExercise =
                exerciseDefinitionRepository.saveAndFlush(exerciseDefinition1);
    }


    @Test
    @WithMockUser(username = "cosmin")
    void getAllExercises_ShouldReturnSystemAndOwnCustomExercises() throws Exception {



        mockMvc.perform(
                get("/api/exercises")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].muscleGroup").value(MuscleGroup.CHEST.name()))
                .andExpect(jsonPath("$[0].archived").value(false))
                .andExpect(jsonPath("$[0].exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$[1].exerciseName").value("Lat Pulldown"))
                .andExpect(jsonPath("$[1].muscleGroup").value(MuscleGroup.BACK.name()))
                .andExpect(jsonPath("$[1].archived").value(false))
                .andExpect(jsonPath("$[1].exerciseType").value(ExerciseType.CUSTOM.name()));


    }

    @Test
    @WithMockUser(username = "cosmin")
    void getAllExercises_ShouldNotReturnOtherUsersCustomExercises() throws Exception {

        mockMvc.perform(
                        get("/api/exercises")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].muscleGroup").value(MuscleGroup.CHEST.name()))
                .andExpect(jsonPath("$[0].archived").value(false))
                .andExpect(jsonPath("$[0].exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$[1].exerciseName").value("Lat Pulldown"))
                .andExpect(jsonPath("$[1].muscleGroup").value(MuscleGroup.BACK.name()))
                .andExpect(jsonPath("$[1].archived").value(false))
                .andExpect(jsonPath("$[1].exerciseType").value(ExerciseType.CUSTOM.name()));


    }

    @Test
    @WithMockUser(username = "cosmin")
    void getAllExercises_ShouldNotReturnArchivedExercises() throws Exception {

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bicep Curls");
        exerciseDefinition.setMuscleGroup(MuscleGroup.ARMS);
        exerciseDefinition.setExerciseType(ExerciseType.CUSTOM);
        exerciseDefinition.setArchived(true);
        exerciseDefinition.setNormalizedName("bicep curls");
        exerciseDefinition.setOwner(userRepository.findByUsername("cosmin").get());

        exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);

        mockMvc.perform(
                        get("/api/exercises")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].muscleGroup").value(MuscleGroup.CHEST.name()))
                .andExpect(jsonPath("$[0].archived").value(false))
                .andExpect(jsonPath("$[0].exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$[1].exerciseName").value("Lat Pulldown"))
                .andExpect(jsonPath("$[1].muscleGroup").value(MuscleGroup.BACK.name()))
                .andExpect(jsonPath("$[1].archived").value(false))
                .andExpect(jsonPath("$[1].exerciseType").value(ExerciseType.CUSTOM.name()));


    }


    @Test
    @WithMockUser(username = "cosmin")
    void getExerciseById_ShouldReturnSystemExercise() throws Exception {
        mockMvc.perform(
                        get("/api/exercises/" + systemExercise.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.muscleGroup").value("CHEST"))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.exerciseType").value("SYSTEM"));
    }

    @Test
    @WithMockUser(username = "cosmin")
    void getExerciseById_ShouldReturn404ForOtherUsersCustomExercise() throws Exception {


        mockMvc.perform(
                get("/api/exercises/3")
        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser("cosmin")
    void getExerciseById_ShouldReturn404ForArchivedExercise() throws Exception {

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bicep Curls");
        exerciseDefinition.setMuscleGroup(MuscleGroup.ARMS);
        exerciseDefinition.setExerciseType(ExerciseType.CUSTOM);
        exerciseDefinition.setArchived(true);
        exerciseDefinition.setNormalizedName("bicep curls");
        exerciseDefinition.setOwner(userRepository.findByUsername("cosmin").get());


        exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);

        mockMvc.perform(
                get("/api/exercises/4")
        )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "cosmin")
    void createExercise_ShouldCreateCustomExerciseOwnedByCurrentUser() throws Exception {

        mockMvc.perform(
                post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(
                                """
                                        {
                                        "exerciseName": "Bicep Curls",
                                        "muscleGroup": "ARMS"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName").value("Bicep Curls"))
                .andExpect(jsonPath("$.muscleGroup").value(MuscleGroup.ARMS.name()))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.exerciseType").value(ExerciseType.CUSTOM.name()));

    }

    @Test
    @WithMockUser(username = "cosmin")
    void createExercise_ShouldCleanAndNormalizeName() throws Exception {

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content(
                                        """
                                                {
                                                "exerciseName": "Bicep     Curls",
                                                "muscleGroup": "ARMS"
                                                }
                                                """
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName").value("Bicep Curls"))
                .andExpect(jsonPath("$.muscleGroup").value(MuscleGroup.ARMS.name()))
                .andExpect(jsonPath("$.archived").value(false))
                .andExpect(jsonPath("$.exerciseType").value(ExerciseType.CUSTOM.name()));

    }

    @Test
    @WithMockUser(username = "cosmin")
    void createExercise_ShouldRejectDuplicateOwnCustomName() throws Exception {

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content(
                                        """
                                                {
                                                "exerciseName": "Lat Pulldown",
                                                "muscleGroup": "BACK"
                                                }
                                                """
                                )
                )
                .andDo(print())
                .andExpect(status().isConflict());

    }

    @Test
    @WithMockUser(username = "cosmin")
    void createExercise_ShouldRejectDuplicateSystemName() throws Exception {

        mockMvc.perform(
                        post("/api/exercises")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content(
                                        """
                                                {
                                                "exerciseName": "Bench Press",
                                                "muscleGroup": "Chest"
                                                }
                                                """
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(username = "cosmin")
    void updateExercise_ShouldUpdateOwnCustomExerciseAndNormalizedName()
            throws Exception {

        mockMvc.perform(
                        put("/api/exercises/" + cosminExercise.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "exerciseName": "  Wide   Row  ",
                              "muscleGroup": "BACK"
                            }
                            """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseName").value("Wide Row"))
                .andExpect(jsonPath("$.muscleGroup").value("BACK"))
                .andExpect(jsonPath("$.exerciseType").value("CUSTOM"));

        ExerciseDefinition updated =
                exerciseDefinitionRepository
                        .findById(cosminExercise.getId())
                        .orElseThrow();

        assertEquals("Wide Row", updated.getName());
        assertEquals("wide row", updated.getNormalizedName());
    }

    @Test
    @WithMockUser(username = "cosmin")
    void updateExercise_ShouldRejectSystemExercise() throws Exception {

        mockMvc.perform(
                        put("/api/exercises/" + systemExercise.getId())
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {                                           
                                 "exerciseName": "Wide Row",
                                  "muscleGroup": "BACK"    
                                }
                                """
                                )
                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(username = "cosmin")
    void updateExercise_ShouldRejectOtherUsersCustomExercise() throws Exception {

        mockMvc.perform(
                        put("/api/exercises/" + ionutExercise.getId())
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {  
                                "exerciseName": "Wide Row",
                                "muscleGroup": "BACK"                                               
                                }
                                """)
                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(username = "cosmin")
    void updateExercise_ShouldRejectDuplicateNormalizedName()
    throws Exception {

        mockMvc.perform(
                        put("/api/exercises/" + cosminExercise.getId())
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {                                      
                                "exerciseName": "Lat Pulldown",
                               "muscleGroup": "BACK"
                                }
                                """)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }


    @Test
    @WithMockUser(username = "cosmin")
    void archiveExercise_ShouldArchiveOwnCustomExercise() throws Exception {


        mockMvc.perform(
                patch("/api/exercises/" +  cosminExercise.getId())
        )
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "cosmin")
    void archiveExercise_ShouldRejectSystemExercise() throws Exception {


        mockMvc.perform(
                        patch("/api/exercises/" + systemExercise.getId())
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "cosmin")
    void archiveExercise_ShouldRejectOtherUsersCustomExercise() throws Exception {


        mockMvc.perform(
                        patch("/api/exercises/" + ionutExercise.getId())
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
