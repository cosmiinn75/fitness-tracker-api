package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.Controller.ProgressController;
import com.cosmin.fitness_tracker_api.DTO.PersonalRecordResponse;
import com.cosmin.fitness_tracker_api.DTO.VolumeProgressResponse;
import com.cosmin.fitness_tracker_api.DTO.WorkoutVolumeResponse;
import com.cosmin.fitness_tracker_api.Security.JWTFilter;
import com.cosmin.fitness_tracker_api.Service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

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


}
