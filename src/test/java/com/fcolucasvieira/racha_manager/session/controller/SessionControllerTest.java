package com.fcolucasvieira.racha_manager.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcolucasvieira.racha_manager.player.dto.request.CreatePlayerRequest;
import com.fcolucasvieira.racha_manager.session.dto.request.FinishMatchRequest;
import com.fcolucasvieira.racha_manager.session.enums.MatchResultType;
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
public class SessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String createSessionAndReturnId() throws Exception {
        MvcResult sessionResult = mockMvc.perform(
                post("/sessions")
        ).andReturn();

        String sessionBody = sessionResult.getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionBody)
                .path("data")
                .path("id")
                .asText();

        return sessionId;
    }

    private String createPlayerAndReturnId(String name) throws Exception {
        MvcResult playerResult = mockMvc.perform(
                post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new CreatePlayerRequest(name)
                                )
                        )
        ).andReturn();

        String playerBody = playerResult.getResponse().getContentAsString();

        String playerId = objectMapper.readTree(playerBody)
                .path("data")
                .path("id")
                .asText();

        return playerId;
    }

    // helper (preenchimento de sessão com 8 jogadores)
    private void fillSessionWithEightPlayers(String sessionId) throws Exception {
        for(int i = 0; i < 8; i++) {
            String playerId = createPlayerAndReturnId("P" + i);

            mockMvc.perform(
                    post("/sessions/" + sessionId + "/players/" + playerId)
            );
        }
    }

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
        // cria sessão
        String sessionId = createSessionAndReturnId();

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
                .andExpect(jsonPath("$.message").value("Session not found with Id: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAddPlayerToSessionSuccessfully() throws Exception {
        // cria jogador
        String playerId = createPlayerAndReturnId("Lucas");

        // cria sessão
        String sessionId = createSessionAndReturnId();

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
        String playerId = createPlayerAndReturnId("Lucas");

        // cria sessão
        String sessionId = createSessionAndReturnId();

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
                .andExpect(jsonPath("$.message").value("Player already in session with Id: " + playerId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenSessionDoesNotExistInAddPlayer() throws Exception {
        // ID aleatório para executar endpoint
        String randomSessionId = UUID.randomUUID().toString();

        // cria jogador
        String playerId = createPlayerAndReturnId("Lucas");

        mockMvc.perform(
                post("/sessions/" + randomSessionId + "/players/" + playerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found with Id: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenPlayerDoesNotExist() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // ID aleatório para executar endpoint
        String randomPlayerId = UUID.randomUUID().toString();

        mockMvc.perform(
                post("/sessions/" + sessionId + "/players/" + randomPlayerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Player not found with Id: " + randomPlayerId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldRemovePlayerSuccessfullyWhenExistsEightPlayers() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // variável playerId para adicionar jogadores e, posteriormente, usá-lo para remoção
        String playerId = null;
        
        // adiciona 8 jogadores a sessão (times iniciais criados automaticamente)
        for(int i = 0; i < 8; i++) {
            playerId = createPlayerAndReturnId("P" + i);

            mockMvc.perform(
                    post("/sessions/" + sessionId + "/players/" + playerId)
            );
        }

        mockMvc.perform(
                delete("/sessions/" + sessionId + "/players/" + playerId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Player removed successfully"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenPlayerIsNotInATeam() throws Exception {
        // cria jogador
        String playerId = createPlayerAndReturnId("Lucas");

        // cria sessão
        String sessionId = createSessionAndReturnId();

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
                        .value("Player not found with Id: " + playerId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenRemovingPlayerFromNonExistingSession() throws Exception {
        // ID aleatório para executar endpoint do teste
        String randomSessionId = UUID.randomUUID().toString();

        // cria jogador
        String playerId = createPlayerAndReturnId("Lucas");

        mockMvc.perform(
                delete("/sessions/" + randomSessionId + "/players/" + playerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found with Id: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenRemovingPlayerNotInSession() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // cria e adiciona 8 jogadores a sessão existente (formação de times e início das partidas)
        fillSessionWithEightPlayers(sessionId);

        // ID aleatório para teste do endpoint
        String randomPlayerId = createPlayerAndReturnId("Lucas");

        mockMvc.perform(
                delete("/sessions/" + sessionId + "/players/" + randomPlayerId)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Player not found with Id: " + randomPlayerId))
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
                .andExpect(jsonPath("$.message").value("Session not found with Id: " + randomSessionId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnConflictWhenSessionHasNotStarted() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

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

    @Test
    void shouldReturnBadRequestWhenWinnerTeamNumberIsNull() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // cria e adiciona 8 jogadores a sessão existente (formação de times e início de partidas)
        fillSessionWithEightPlayers(sessionId);

        mockMvc.perform(
                post("/sessions/" + sessionId + "/finish-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        new FinishMatchRequest(null, MatchResultType.WINNER)
                                )
                        )
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Winner team number is required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenDrawHasWinnerTeam() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // cria e adiciona 8 jogadores a sessão existente (formação de times e início de partidas)
        fillSessionWithEightPlayers(sessionId);

        mockMvc.perform(
                        post("/sessions/" + sessionId + "/finish-match")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new FinishMatchRequest(1, MatchResultType.DRAW)
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Winner team number must be null on draw"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFinishMatchWinnerSuccessfully() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // cria e adiciona 8 jogadores a sessão existentes (formação de times e início dos jogos)
        fillSessionWithEightPlayers(sessionId);

        mockMvc.perform(
                        post("/sessions/" + sessionId + "/finish-match")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new FinishMatchRequest(1, MatchResultType.WINNER)
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Match finished successfully"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldFinishMatchWithDrawSuccessfully() throws Exception {
        // cria sessão
        String sessionId = createSessionAndReturnId();

        // cria e adiciona 8 jogadores a sessão existente (formação de times e início dos jogos)
        fillSessionWithEightPlayers(sessionId);

        // cria e adiciona mais 8 jogadores a sessão existente (para executar o empate, são necessários 2 times na queue)
        fillSessionWithEightPlayers(sessionId);

        mockMvc.perform(
                        post("/sessions/" + sessionId + "/finish-match")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new FinishMatchRequest(null, MatchResultType.DRAW)
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Match finished successfully"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

