package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.Controller.ExerciseDefinitionController;
import com.cosmin.fitness_tracker_api.Security.JWTFilter;
import com.cosmin.fitness_tracker_api.Service.ExerciseDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExerciseDefinitionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseDefinitionService exerciseDefinitionService;

    @MockitoBean
    private JWTFilter  jwtFilter;



}
