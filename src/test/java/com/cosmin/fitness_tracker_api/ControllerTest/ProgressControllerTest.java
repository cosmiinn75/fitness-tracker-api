package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.Controller.ProgressController;
import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Security.JWTFilter;
import com.cosmin.fitness_tracker_api.Service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private JWTFilter  jwtFilter;


    @Test
    void getWeeklyVolume_ShouldReturnVolumeProgressResponse200() throws Exception {

        VolumeProgressResponse response = new VolumeProgressResponse(
                LocalDate.of(2026,5,7),
                LocalDate.of(2026,5,14),
                120.00
        );

        when(progressService.getWeeklyVolume()).thenReturn(response);

        mockMvc.perform(
                get("/api/progress/weekly-volume")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2026-05-07"))
                .andExpect(jsonPath("$.endDate").value("2026-05-14"))
                .andExpect(jsonPath("$.volume").value(120.00));

        verify(progressService).getWeeklyVolume();
    }


    @Test
    void getMonthlyVolume_ShouldReturnVolumeProgressResponse200() throws Exception {

        VolumeProgressResponse response = new VolumeProgressResponse(
                LocalDate.of(2026,5,1),
                LocalDate.of(2026,5,7),
                120.00
        );

        when(progressService.getMonthlyVolume()).thenReturn(response);

        mockMvc.perform(
                        get("/api/progress/monthly-volume")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2026-05-01"))
                .andExpect(jsonPath("$.endDate").value("2026-05-07"))
                .andExpect(jsonPath("$.volume").value(120.00));

        verify(progressService).getMonthlyVolume();
    }


    @Test
    void getWorkoutVolume_ShouldReturnWorkoutVolumeResponse200() throws Exception {

        WorkoutVolumeResponse response = new WorkoutVolumeResponse(
                1000.00
        );

        when(progressService.getWorkoutVolumeById(1L)).thenReturn(response);

        mockMvc.perform(
                get("/api/progress/workouts/1/volume")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVolume").value(1000.00));

        verify(progressService).getWorkoutVolumeById(1L);
    }

    @Test
    void getPersonalRecord_ShouldReturnPersonalRecordResponse200() throws Exception {
        PersonalRecordResponse response = new PersonalRecordResponse(
                1L,
                "Bench Press",
                100.00,
                5,
                1,
                LocalDate.of(2026,5,7)
        );

        when(progressService.getPersonalRecordByExerciseDefinitionId(1L)).thenReturn(response);

        mockMvc.perform(
                get("/api/progress/exercises/1/personal-record")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseDefinitionId").value(1))
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.weight").value(100.00))
                .andExpect(jsonPath("$.reps").value(5))
                .andExpect(jsonPath("$.date").value(LocalDate.of(2026,5,7).toString()));

        verify(progressService).getPersonalRecordByExerciseDefinitionId(1L);
    }

    @Test
    void getWorkoutHistory_ShouldReturnPagedHistoryResponse200() throws Exception {

        Long exerciseDefinitionId = 1L;

        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = LocalDate.of(2026, 7, 20);

        SetResponse setResponse = new SetResponse(
                1L,
                1,
                100.0,
                5,
                1
        );

        WorkoutExerciseHistoryResponse historyResponse =
                new WorkoutExerciseHistoryResponse(
                        10L,
                        20L,
                        1,
                        "Bench Press",
                        LocalDate.of(2026, 7, 15),
                        List.of(setResponse)
                );

        PagedResponse<WorkoutExerciseHistoryResponse> response =
                new PagedResponse<>(
                        List.of(historyResponse),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true
                );

        when(progressService.getWorkoutHistory(
                exerciseDefinitionId,
                startDate,
                endDate,
                0,
                10
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/progress/exercises/1/history")
                                .param("page", "0")
                                .param("size", "10")
                                .param("startDate", "2026-07-10")
                                .param("endDate", "2026-07-20")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].workoutId").value(10))
                .andExpect(jsonPath("$.content[0].workoutExerciseId").value(20))
                .andExpect(jsonPath("$.content[0].exerciseNumber").value(1))
                .andExpect(jsonPath("$.content[0].exerciseName")
                        .value("Bench Press"))
                .andExpect(jsonPath("$.content[0].workoutDate")
                        .value("2026-07-15"))
                .andExpect(jsonPath("$.content[0].setResponses.length()")
                        .value(1))
                .andExpect(jsonPath("$.content[0].setResponses[0].id")
                        .value(1))
                .andExpect(jsonPath("$.content[0].setResponses[0].setNumber")
                        .value(1))
                .andExpect(jsonPath("$.content[0].setResponses[0].weight")
                        .value(100.0))
                .andExpect(jsonPath("$.content[0].setResponses[0].reps")
                        .value(5))
                .andExpect(jsonPath("$.content[0].setResponses[0].rir")
                        .value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(progressService).getWorkoutHistory(
                exerciseDefinitionId,
                startDate,
                endDate,
                0,
                10
        );
    }


}
