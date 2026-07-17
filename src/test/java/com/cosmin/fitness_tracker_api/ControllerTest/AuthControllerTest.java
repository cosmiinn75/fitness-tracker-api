package com.cosmin.fitness_tracker_api.ControllerTest;

import com.cosmin.fitness_tracker_api.Controller.AuthController;
import com.cosmin.fitness_tracker_api.DTO.AuthResponse;
import com.cosmin.fitness_tracker_api.Security.JWTFilter;
import com.cosmin.fitness_tracker_api.Service.AuthService;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @Test
    void register_WithValidCredentials_ShouldReturnTokens() throws Exception {

            AuthResponse authResponse = new AuthResponse(
                    "accessToken",
                    "refreshToken"
            );

            when(authService.register(any())).thenReturn(authResponse);

            mockMvc.perform(
                    post("/api/auth/register").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                            .content("""
                                    {
                                        "username": "cosmin",
                                        "email": "cosmin@gmail.com",
                                        "password": "parola1234"
                                    }
                                    """)


            )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("accessToken"))
                    .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

            verify(authService).register(any());

    }


    @Test
    void register_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(
                post("/api/auth/register").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                   {
                                        "username": "",
                                        "email": "invallid",
                                        "password": ""
                                    }       
                                    """
                        )

        ).andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService,never()).register(any());

    }


    @Test
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {

        AuthResponse authResponse = new AuthResponse(
                "accessToken",
                "refreshToken"
        );

        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(
                post("/api/auth/login").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                        {
                                        "username": "cosmin",
                                        "password": "parola1234"
                                        }
                                        """
                        )
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

        verify(authService).login(any());

    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(
                post("/api/auth/login").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                        {
                                        "username": "",
                                        "password": ""
                                        }
                                        """
                        )
        ).andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService,never()).login(any());
    }

    @Test
    void refresh_WithValidToken_ShouldReturnTokens() throws Exception {

        AuthResponse authResponse = new AuthResponse(
                "newAccessToken",
                "newRefreshToken"
        );

        when(authService.refresh(any())).thenReturn(authResponse);

        mockMvc.perform(
                post("/api/auth/refresh").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                        {
                                        "refreshToken": "oldRefreshToken"
                                        }
                                        """
                        )
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));

                verify(authService).refresh(
                        argThat(
                                request -> request.refreshToken().equals("oldRefreshToken")
                        )
                );

    }


    @Test
    void refresh_WithInvalidToken_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(
                post("/api/auth/refresh").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                        {
                                        "refreshToken": ""
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isBadRequest());


        verify(authService,never()).refresh(any());
    }


    @Test
    void logout_WithValidToken_ShouldReturnVoidAnd200() throws Exception {


        mockMvc.perform(
                post("/api/auth/logout").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                        {
                                        "refreshToken": "oldRefreshToken"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isNoContent());


        verify(authService).logout(
                argThat( request -> request.refreshToken().equals("oldRefreshToken"))
        );

    }

    @Test
    void logout_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(
                post("/api/auth/logout").contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(
                                """
                                        {
                                        "refreshToken": ""
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService,never()).logout(any());
    }

}
