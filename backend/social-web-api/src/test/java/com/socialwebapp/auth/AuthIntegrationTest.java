// src/test/java/com/socialwebapp/auth/AuthIntegrationTest.java
package com.socialwebapp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuthIntegrationTest {

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 👈 no Spring bean

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    void register_then_login_returns_accessToken() throws Exception {

        String email = "it+" + UUID.randomUUID() + "@example.com";
        String password = "Password123!";

        var registerBody = objectMapper.writeValueAsString(
                new RegisterRequest(email, password)
        );

        var loginBody = objectMapper.writeValueAsString(
                new LoginRequest(email, password)
        );

        // Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email));

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken",
                        not(isEmptyOrNullString())));
    }

    private record RegisterRequest(String email, String password) {}
    private record LoginRequest(String email, String password) {}
}