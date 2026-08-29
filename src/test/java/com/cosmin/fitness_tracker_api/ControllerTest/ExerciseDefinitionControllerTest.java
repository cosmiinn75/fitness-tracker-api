package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.controller.ExerciseDefinitionController;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionRequest;
import com.cosmin.fitness_tracker_api.DTO.ExerciseDefinitionResponse;
import com.cosmin.fitness_tracker_api.Enum.ExerciseType;
import com.cosmin.fitness_tracker_api.Enum.MuscleGroup;
import com.cosmin.fitness_tracker_api.security.JWTFilter;
import com.cosmin.fitness_tracker_api.service.ExerciseDefinitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ExerciseDefinitionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ExerciseDefinitionControllerTest extends BaseControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseDefinitionService exerciseDefinitionService;


    @Test
    void getAllExercises_ShouldReturnAllExercises() throws Exception {

        ExerciseDefinitionResponse exerciseDefinitionResponse =
                new ExerciseDefinitionResponse(
                        1L,
                        "Bench Press",
                        MuscleGroup.CHEST,
                        ExerciseType.SYSTEM,
                        false
                );

        List<ExerciseDefinitionResponse> response =
                List.of(exerciseDefinitionResponse);

        when(exerciseDefinitionService.findAllExerciseDefinitions())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/exercises")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].muscleGroup").value("CHEST"))
                .andExpect(jsonPath("$[0].exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$[0].archived").value(false));

        verify(exerciseDefinitionService)
                .findAllExerciseDefinitions();
    }

    @Test
    void getExerciseById_ShouldReturnExerciseDefinition() throws Exception {

        ExerciseDefinitionResponse response = new ExerciseDefinitionResponse(
                1L,
                "Bench Press",
                MuscleGroup.CHEST,
                ExerciseType.SYSTEM,
                false
        );

        when(exerciseDefinitionService.findExerciseDefinitionById(eq(1L)))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/exercises/1")

        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.muscleGroup").value("CHEST"))
                .andExpect(jsonPath("$.exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$.archived").value(false));

        verify(exerciseDefinitionService).findExerciseDefinitionById(eq(1L));

    }


    @Test
    void createExerciseDefinition_ShouldReturnExerciseDefinitionResponse200()  throws Exception {

        ExerciseDefinitionResponse response = new  ExerciseDefinitionResponse(
                1L,
                "Bench Press",
                MuscleGroup.CHEST,
                ExerciseType.SYSTEM,
                false
        );


        when(exerciseDefinitionService.addExerciseDefinition(any(ExerciseDefinitionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/exercises")
                .contentType(APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "exerciseName": "Bench Press",
                                        "muscleGroup": "CHEST"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.muscleGroup").value("CHEST"))
                .andExpect(jsonPath("$.exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$.archived").value(false));

        verify(exerciseDefinitionService).addExerciseDefinition(any(ExerciseDefinitionRequest.class));

    }

    @Test
    void updateExerciseDefinition_ShouldReturnExerciseDefinitionResponse200()  throws Exception {

        ExerciseDefinitionResponse response = new  ExerciseDefinitionResponse(
                1L,
                "Bench Press",
                MuscleGroup.CHEST,
                ExerciseType.SYSTEM,
                false
        );

        when(exerciseDefinitionService.updateExerciseDefinition(eq(1L),any(ExerciseDefinitionRequest.class)))
                .thenReturn(response);


        mockMvc.perform(
                put("/api/exercises/1")
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "exerciseName": "Bench Press",
                                        "muscleGroup": "CHEST"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.muscleGroup").value("CHEST"))
                .andExpect(jsonPath("$.exerciseType").value(ExerciseType.SYSTEM.name()))
                .andExpect(jsonPath("$.archived").value(false));

        verify(exerciseDefinitionService).updateExerciseDefinition(eq(1L),any(ExerciseDefinitionRequest.class));

    }

}
