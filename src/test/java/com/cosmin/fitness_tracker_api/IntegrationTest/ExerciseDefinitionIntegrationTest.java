package com.cosmin.fitness_tracker_api.IntegrationTest;



import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.Model.ExerciseDefinition;
import com.cosmin.fitness_tracker_api.Repository.ExerciseDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class ExerciseDefinitionIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExerciseDefinitionRepository exerciseDefinitionRepository;

    @BeforeEach
    void setUp() {
        exerciseDefinitionRepository.deleteAll();

        ExerciseDefinition exerciseDefinition = new ExerciseDefinition();
        exerciseDefinition.setName("Bench Press");
        exerciseDefinition.setMuscleGroup(MuscleGroup.CHEST);

        exerciseDefinitionRepository.saveAndFlush(exerciseDefinition);
    }


    @Test
    @WithMockUser
    void getAllExercises_ShouldReturnExercisesFromDatabase() throws Exception {

        mockMvc.perform(
                get("/api/exercises")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].muscleGroup").value(MuscleGroup.CHEST.name()));


    }
}
