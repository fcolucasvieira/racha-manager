package com.fcolucasvieira.racha_manager.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcolucasvieira.racha_manager.player.dto.request.CreatePlayerRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PlayerControllerTest {
    // Simula chamadas HTTP sem precisar subir servidor Tomcat
    @Autowired
    private MockMvc mockMvc;

    // Utilizado para converter objetos Java em JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreatePlayerSuccessfully() throws Exception {
        CreatePlayerRequest request = new CreatePlayerRequest("Player");

        mockMvc.perform(
                post("/players")
                        .contentType(APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Player created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.timestamp").exists());

    }

    @Test
    void shouldRejectBlankName() throws Exception {
        CreatePlayerRequest request = new CreatePlayerRequest("");

        mockMvc.perform(
                post("/players")
                        .contentType(APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRejectNullName() throws Exception {
        CreatePlayerRequest request = new CreatePlayerRequest(null);

        mockMvc.perform(
                post("/players")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
