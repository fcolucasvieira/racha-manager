package com.fcolucasvieira.racha_manager.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerRequest;
import com.fcolucasvieira.racha_manager.application.dto.FinishMatchRequest;
import com.fcolucasvieira.racha_manager.domain.enums.MatchResultType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SessionControllerIT {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateSessionSuccessfully() throws Exception {
        mockMvc.perform(
                post("/sessions")
                        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Session created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldGetSessionSuccessfully() throws Exception {
        MvcResult createResult =
                mockMvc.perform(post("/sessions")).andReturn();

        String body =
                createResult.getResponse()
                        .getContentAsString();

        JsonNode json = objectMapper.readTree(body);

        String sessionId = json.path("data")
                .path("id")
                .asText();

        mockMvc.perform(
                get("/sessions/" + sessionId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Session retrieved successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(sessionId));
    }

    @Test
    void shouldReturnNotFoundWhenSessionDoesNotExist() throws Exception {
        String randomSessionId = UUID.randomUUID().toString();

        mockMvc.perform(
                get("/sessions/" + randomSessionId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAddPlayerToSessionSuccessfully() throws Exception {
        // cria jogador
        MvcResult playerResult = mockMvc.perform(
                post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new CreatePlayerRequest("Player")
                                )
                        )
        ).andReturn();

        String playerBody =
                playerResult.getResponse()
                        .getContentAsString();

        String playerId =
                objectMapper.readTree(playerBody)
                        .path("data")
                        .path("id")
                        .asText();

        // cria sessão
        MvcResult sessionResult = mockMvc.perform(
                post("/sessions")
        ).andReturn();

        String sessionBody = sessionResult
                .getResponse().getContentAsString();

        String sessionId =
                objectMapper.readTree(sessionBody)
                        .path("data")
                        .path("id")
                        .asText();

        // adiciona jogador a sessão, valida cenários
        mockMvc.perform(
                post("/sessions/" + sessionId + "/players/" + playerId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Player added successfully"));
    }

    @Test
    void shouldReturnConflictWhenAddingSamePlayerTwice() throws Exception {
        // cria jogador
        MvcResult playerResult = mockMvc.perform(
                post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                new CreatePlayerRequest("Player")
                                )
                        )
        ).andReturn();

        String playerBody = playerResult
                .getResponse().getContentAsString();

        String playerId = objectMapper.readTree(playerBody)
                .path("data")
                .path("id")
                .asText();

        // cria sessão
        MvcResult sessionResult = mockMvc.perform(
                post("/sessions")
        ).andReturn();

        String sessionBody = sessionResult
                .getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionBody)
                .path("data")
                .path("id")
                .asText();

        // adiciona jogador (sucesso)
        // não validamos cenários aqui, pois já sabemos o seu resultado em testes anteriores
        mockMvc.perform(
                post("/sessions/" + sessionId + "/players/" + playerId)
        )
                .andExpect(status().isOk());

        mockMvc.perform(
                post("/sessions/" + sessionId + "/players/" + playerId)
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Player already in session"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenSessionDoesNotExistInAddPlayer() throws Exception {
        // ID aleatório para executar endpoint
        String randomSessionId = UUID.randomUUID().toString();

        // cria jogador
        MvcResult playerResult = mockMvc.perform(
                post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                new CreatePlayerRequest("Lucas")
                                )
                        )
        ).andReturn();

        String playerBody = playerResult
                .getResponse().getContentAsString();

        String playerId = objectMapper.readTree(playerBody)
                .path("data")
                .path("id")
                .asText();


        mockMvc.perform(
                post("/sessions/" + randomSessionId + "/players/" + playerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenPlayerDoesNotExist() throws Exception {
        // cria sessão
        MvcResult sessionResult = mockMvc.perform(
                post("/sessions")
        ).andReturn();

        String sessionBody = sessionResult
                .getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionBody)
                .path("data")
                .path("id")
                .asText();

        // ID aleatório para executar endpoint
        String randomPlayerId = UUID.randomUUID().toString();

        mockMvc.perform(
                post("/sessions/" + sessionId + "/players/" + randomPlayerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Player not found: " + randomPlayerId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // shouldRemovePlayerSuccessfully()
    // depende de formação de times (8 jogadores)

    @Test
    void shouldReturnNotFoundWhenPlayerIsNotInATeam() throws Exception {
        // cria jogador
        MvcResult playerResult = mockMvc.perform(
                post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new CreatePlayerRequest("Player")
                                )
                        )
        ).andReturn();

        String playerBody = playerResult
                .getResponse().getContentAsString();

        String playerId = objectMapper.readTree(playerBody)
                .path("data")
                .path("id")
                .asText();

        // cria sessão
        MvcResult sessionResult = mockMvc.perform(
                post("/sessions")
        ).andReturn();

        String sessionBody = sessionResult
                .getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionBody)
                .path("data")
                .path("id")
                .asText();

        // adicionar jogador (útil para possível remoção)
        mockMvc.perform(
                post("/sessions/" + sessionId + "/players/" + playerId)
        ).andExpect(status().isOk());

        mockMvc.perform(
                delete("/sessions/"+ sessionId + "/players/" + playerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Player is not in a team"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenRemovingPlayerFromNonExistingSession() throws Exception {
        // ID aleatório para executar endpoint do teste
        String randomSessionId = UUID.randomUUID().toString();

        // cria jogador
        MvcResult playerResult = mockMvc.perform(
                post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new CreatePlayerRequest("Player")
                                )
                        )
        ).andReturn();

        String playerBody = playerResult
                .getResponse().getContentAsString();

        String playerId = objectMapper.readTree(playerBody)
                .path("data")
                .path("id")
                .asText();

        mockMvc.perform(
                delete("/sessions/" + randomSessionId + "/players/" + playerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenFinishingMatchInNonExistingSession() throws Exception {
        // ID aleatório para realização de teste do endpoint
        String randomSessionId = UUID.randomUUID().toString();

        mockMvc.perform(
                post("/sessions/" + randomSessionId + "/finish-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new FinishMatchRequest(1, MatchResultType.WINNER)
                                )
                                )
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnConflictWhenSessionHasNotStarted() throws Exception {
        // cria sessão
        MvcResult sessionResult = mockMvc.perform(
                post("/sessions")
        ).andReturn();

        String sessionBody = sessionResult.getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionBody)
                .path("data")
                .path("id")
                .asText();

        // obs: sessão existente, mas não iniciada (ideal para o nosso teste)

        mockMvc.perform(
                post("/sessions/" + sessionId + "/finish-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new FinishMatchRequest(1, MatchResultType.WINNER)
                                )
                        )
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session has not started"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
