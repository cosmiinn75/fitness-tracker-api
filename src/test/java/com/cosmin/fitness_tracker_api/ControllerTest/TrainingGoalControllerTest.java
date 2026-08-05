package com.cosmin.fitness_tracker_api.ControllerTest;


import com.cosmin.fitness_tracker_api.controller.TrainingGoalController;
import com.cosmin.fitness_tracker_api.DTO.PagedResponse;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalRequest;
import com.cosmin.fitness_tracker_api.DTO.TrainingGoalResponse;
import com.cosmin.fitness_tracker_api.Enum.Status;
import com.cosmin.fitness_tracker_api.security.JWTFilter;
import com.cosmin.fitness_tracker_api.service.TrainingGoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TrainingGoalController.class)
public class TrainingGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingGoalService trainingGoalService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @Test
    public void createTrainingGoal_ShouldReturnTrainingGoalResponse() throws Exception {
        TrainingGoalResponse response = new TrainingGoalResponse(
                1L,
                "Bench Press",
                100.00,
                5,
                LocalDate.of(3000,5,7),
                Status.ACTIVE
        );

        when(trainingGoalService.createTrainingGoal(any(TrainingGoalRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/training-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {
                                          "exerciseDefinitionId": 1,
                                          "targetWeight": 100.00,
                                          "targetReps": 5,
                                          "targetDate": "3000-05-07"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.targetWeight").value(100.0))
                .andExpect(jsonPath("$.targetReps").value(5))
                .andExpect(jsonPath("$.targetDate").value(LocalDate.of(3000,5,7).toString()));

        verify(trainingGoalService).createTrainingGoal(any(TrainingGoalRequest.class))     ;

    }


    @Test
    void cancelTrainingGoal_ShouldReturnTrainingGoalResponse() throws Exception {
        TrainingGoalResponse response = new TrainingGoalResponse(
                1L,
                "Bench Press",
                100.00,
                5,
                LocalDate.of(3000,5,7),
                Status.CANCELLED
        );

        when(trainingGoalService.cancelTrainingGoal(any())).thenReturn(response);

        mockMvc.perform(
                patch("/api/training-goals/1/cancel")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.targetWeight").value(100.0))
                .andExpect(jsonPath("$.targetReps").value(5))
                .andExpect(jsonPath("$.targetDate").value(LocalDate.of(3000,5,7).toString()));


        verify(trainingGoalService).cancelTrainingGoal(any());
    }


    @Test
    void getTrainingGoals_WithValidPageAndSize_ShouldReturnPagedTrainingGoals()
            throws Exception {

        TrainingGoalResponse goal1 = new TrainingGoalResponse(
                1L,
                "Bench Press",
                100.0,
                5,
                LocalDate.of(3000, 5, 7),
                Status.ACTIVE
        );

        TrainingGoalResponse goal2 = new TrainingGoalResponse(
                2L,
                "Lat Pulldown",
                80.0,
                10,
                LocalDate.of(3000, 6, 10),
                Status.ACTIVE
        );

        PagedResponse<TrainingGoalResponse> pagedResponse =
                new PagedResponse<>(
                        List.of(goal1, goal2),
                        0,
                        2,
                        2,
                        1,
                        true,
                        true
                );

        when(trainingGoalService.getTrainingGoals(0, 2))
                .thenReturn(pagedResponse);

        mockMvc.perform(
                        get("/api/training-goals")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].exerciseName")
                        .value("Bench Press"))
                .andExpect(jsonPath("$.content[0].targetWeight")
                        .value(100.0))
                .andExpect(jsonPath("$.content[0].targetReps")
                        .value(5))
                .andExpect(jsonPath("$.content[0].status")
                        .value("ACTIVE"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].exerciseName")
                        .value("Lat Pulldown"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(trainingGoalService)
                .getTrainingGoals(0, 2);
    }

}
