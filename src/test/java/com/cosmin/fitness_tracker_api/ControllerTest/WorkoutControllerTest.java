package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.Controller.WorkoutController;
import com.cosmin.fitness_tracker_api.DTO.*;
import com.cosmin.fitness_tracker_api.Security.JWTFilter;
import com.cosmin.fitness_tracker_api.Service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest(controllers = WorkoutController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WorkoutControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WorkoutService workoutService;

    @MockitoBean
    JWTFilter jwtFilter;

    @Test
    void getAllWorkouts_ShouldReturnPagedResponse() throws Exception {

        List<WorkoutResponse> workoutResponseList = new ArrayList<>();

        PagedResponse<WorkoutResponse> response = new PagedResponse<>(
                workoutResponseList,
                0,
                20,
                0,
                1,
                true,
                true
        );

        when(workoutService.getAllWorkoutsFiltered(eq(0),eq(20),isNull(), isNull(), isNull()))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/workouts")
                        .param("page", "0")
                .param("size", "20")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(response.page()))
                .andExpect(jsonPath("$.size").value(response.size()))
                .andExpect(jsonPath("$.totalPages").value(response.totalPages()))
                .andExpect(jsonPath("$.totalElements").value(response.totalElements()))
                .andExpect(jsonPath("$.first").value(response.first()))
                .andExpect(jsonPath("$.last").value(response.last()));

        verify(workoutService).getAllWorkoutsFiltered(eq(0),eq(20),isNull(), isNull(), isNull());
    }


    @Test
    void getWorkoutById_ShouldReturnWorkoutResponse() throws Exception {

        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Push",
                LocalDate.now(),
                exerciseResponseList
        );

        when(workoutService.getWorkoutById(1L)).thenReturn(workoutResponse);

        mockMvc.perform(
                get("/api/workouts/1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Push"))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.exerciseResponses").isArray());



        verify(workoutService).getWorkoutById(1L);
    }


    @Test
    void deleteWorkoutById_ShouldDeleteWorkoutResponse() throws Exception {

        mockMvc.perform(
                delete("/api/workouts/1")
        )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(workoutService).deleteWorkoutById(1L);

    }

    @Test
    void updateWorkoutMetadata_ShouldUpdateWorkoutResponse() throws Exception {

        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026,7,5),
                exerciseResponseList
        );



        when(workoutService.updateWorkoutMetaData(any(WorkoutMetaDataRequest.class),eq(1L))).thenReturn(workoutResponse);

        mockMvc.perform(
                patch("/api/workouts/1")
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "workoutName": "Pull",
                                        "date": "2026-07-05"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray());

        verify(workoutService).updateWorkoutMetaData(any(),eq(1L));


    }


    @Test
    void updateWorkout_ShouldUpdateWorkoutResponse() throws Exception {

        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.replaceWorkout(
                eq(1L),
                any(WorkoutRequest.class)
        )).thenReturn(workoutResponse);

        mockMvc.perform(
                        put("/api/workouts/1")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                    {
                                      "workoutName": "Pull",
                                      "date": "2026-07-05",
                                      "exerciseRequests": [
                                        {
                                          "exerciseDefinitionId": 1,
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
                                    """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray())
                .andExpect(jsonPath("$.exerciseResponses").isEmpty());

        verify(workoutService).replaceWorkout(
                eq(1L),
                any(WorkoutRequest.class)
        );
    }


    @Test
    void createWorkout_ShouldCreateWorkoutResponse() throws Exception {
        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();
        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.createWorkout(any(WorkoutRequest.class))).thenReturn(workoutResponse);

        mockMvc.perform(
                post("/api/workouts")
                .contentType(APPLICATION_JSON)
                        .content(
                                """
                                          {
                                            "workoutName": "Pull",
                                            "date": "2026-07-05",
                                            "exerciseRequests": [
                                              {
                                                "exerciseDefinitionId": 1,
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
                                          """
                        )
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray())
                .andExpect(jsonPath("$.exerciseResponses").isEmpty());

        verify(workoutService).createWorkout(any());

    }

    @Test
    void changeWorkoutExercises_ShouldChangeWorkoutResponse() throws Exception {
        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.changeWorkoutExercise(any(ChangeWorkoutExerciseRequest.class), eq(1L),eq(1))).thenReturn(workoutResponse);

        mockMvc.perform(
                patch("/api/workouts/1/exercises/1")
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "exerciseDefinitionId": 2
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray())
                .andExpect(jsonPath("$.exerciseResponses").isEmpty());

        verify(workoutService).changeWorkoutExercise(any(),eq(1L),eq(1));


    }

    @Test
    void addSet_ShouldAddSetAndReturnWorkoutResponse() throws Exception {
        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.addSet(any(SetRequest.class),eq(1L),eq(1))).thenReturn(workoutResponse);

        mockMvc.perform(
                post("/api/workouts/1/exercises/1/sets")
                .contentType(APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "weight": 100.00,
                                        "reps": 5,
                                        "rir": 1
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray())
                .andExpect(jsonPath("$.exerciseResponses").isEmpty());

        verify(workoutService).addSet(any(),eq(1L),eq(1));
    }


    @Test
    void deleteSet_ShouldDeleteSetAndReturnWorkoutResponse() throws Exception {
        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.deleteExerciseSet(eq(1L),eq(1),eq(1))).thenReturn(workoutResponse);


        mockMvc.perform(
                delete("/api/workouts/1/exercises/1/sets/1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray());

            verify(workoutService).deleteExerciseSet(any(),eq(1),eq(1));
    }

    @Test
    void addWorkoutExercise_ShouldAddWorkoutExerciseAndReturnWorkoutResponse() throws Exception {
        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.addWorkoutExercise(eq(1L),any(ExerciseRequest.class))).thenReturn(workoutResponse);

        mockMvc.perform(
                        post("/api/workouts/1/exercises")
                                .contentType(APPLICATION_JSON)
                                .content(
                                        """
                                                {
                                                "exerciseDefinitionId": 2,
                                                "setRequests": [
                                                    {
                                                     "weight": 100.00,
                                                    "reps": 5,
                                                    "rir": 2
                                                    }
                                                ]
                                                }
                                                """
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray())
                .andExpect(jsonPath("$.exerciseResponses").isEmpty());

        verify(workoutService).addWorkoutExercise(eq(1L),any(ExerciseRequest.class));
    }


    @Test
    void deleteWorkoutExercise_ShouldDeleteWorkoutExerciseAndReturnWorkoutResponse() throws Exception {
        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.deleteWorkoutExercise(eq(1L),eq(1))).thenReturn(workoutResponse);


        mockMvc.perform(
                        delete("/api/workouts/1/exercises/1")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workoutName").value("Pull"))
                .andExpect(jsonPath("$.date").value("2026-07-05"))
                .andExpect(jsonPath("$.exerciseResponses").isArray());

        verify(workoutService).deleteWorkoutExercise(eq(1L),eq(1));
    }

    @Test
    void duplicateWorkout_ShouldReturnWorkoutResponse() throws Exception {

        List<ExerciseResponse> exerciseResponseList = new ArrayList<>();

        WorkoutResponse workoutResponse = new WorkoutResponse(
                1L,
                "Pull",
                LocalDate.of(2026, 7, 5),
                exerciseResponseList
        );

        when(workoutService.duplicateWorkout(any(DuplicateWorkoutRequest.class),eq(1L))).thenReturn(workoutResponse);

        mockMvc.perform(
                post("/api/workouts/1/duplicate")
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "date": "2026-07-05",
                                        "workoutName": "Pull"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutName").value( workoutResponse.workoutName()))
                .andExpect(jsonPath("$.date").value(workoutResponse.date().toString()));

        verify(workoutService).duplicateWorkout(any(),eq(1L));
    }




}
