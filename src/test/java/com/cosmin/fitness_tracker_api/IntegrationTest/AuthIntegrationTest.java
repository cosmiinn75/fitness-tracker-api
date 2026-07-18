package com.cosmin.fitness_tracker_api.IntegrationTest;

import com.cosmin.fitness_tracker_api.Model.RefreshToken;
import com.cosmin.fitness_tracker_api.Model.User;
import com.cosmin.fitness_tracker_api.Repository.RefreshTokenRepository;
import com.cosmin.fitness_tracker_api.Repository.UserRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void setup() {

        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldReturnTokens() throws Exception {


        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {
                                        "username": "cosmin",
                                        "email": "cosmin@gmail.com",
                                        "password": "parola1234"
                                        }
                                        """
                        )

        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());


        User savedUser = userRepository.findByUsername("cosmin")
                .orElseThrow();

        assertEquals("cosmin",savedUser.getUsername());
        assertEquals("cosmin@gmail.com",savedUser.getEmail());
        assertTrue(passwordEncoder.matches("parola1234", savedUser.getPassword()));
    }

    @Test
    void login_ShouldReturnTokens() throws Exception {

        User user = new User();
        user.setUsername("cosmin");
        user.setEmail("cosmin@gmail.com");
        user.setPassword(passwordEncoder.encode("parola1234"));

        userRepository.saveAndFlush(user);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                              {
                                        "username": "cosmin",
                                        "password": "parola1234"
                                        }
                                        """
                        )
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertTrue(userRepository.findByUsername("cosmin").isPresent());

    }


    @Test
    @WithMockUser(username = "cosmin")
    void logout_ShouldRevokeRefreshToken() throws Exception {

        User user = new User();
        user.setUsername("cosmin");
        user.setEmail("cosmin@gmail.com");
        user.setPassword(passwordEncoder.encode("parola1234"));

        user = userRepository.saveAndFlush(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("oldToken");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS)
        );

        refreshTokenRepository.saveAndFlush(refreshToken);

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "refreshToken": "oldToken"
                                    }
                                    """)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        assertTrue(
                refreshTokenRepository.findByToken("oldToken").get().isRevoked()
        );
    }

    @Test
    @WithMockUser(username = "cosmin")
    void refresh_ShouldReturnTokens() throws Exception {
        User user = new User();
        user.setUsername("cosmin");
        user.setPassword(passwordEncoder.encode("parola1234"));
        user.setEmail("cosmin@gmail.com");

        user = userRepository.saveAndFlush(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("oldToken");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.saveAndFlush(refreshToken);

        mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                "refreshToken": "oldToken"
                                }
                """)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertTrue(userRepository.findByUsername("cosmin").isPresent());
        assertTrue(refreshTokenRepository.findByToken("oldToken").get().isRevoked());

    }



}
