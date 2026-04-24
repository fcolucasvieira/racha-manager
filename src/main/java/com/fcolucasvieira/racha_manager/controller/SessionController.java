package com.fcolucasvieira.racha_manager.controller;

import com.fcolucasvieira.racha_manager.dto.SessionActivePlayerResponse;
import com.fcolucasvieira.racha_manager.dto.SessionTeamResponse;
import com.fcolucasvieira.racha_manager.mapper.SessionMapper;
import com.fcolucasvieira.racha_manager.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final CreateSessionUseCase createUseCase;
    private final AddPlayerToSessionUseCase addPlayerUseCase;
    private final GetTeamsInSessionUseCase getTeamsUseCase;
    private final GetActivePlayersUseCase getActivePlayersUseCase;
    private final RemovePlayerFromSessionUseCase removePlayerUseCase;
    private final SessionMapper sessionMapper;

    @PostMapping
    public ResponseEntity<UUID> createSession() {
        UUID response = createUseCase.execute();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{sessionId}/players/{playerId}")
    public ResponseEntity<List<SessionTeamResponse>> addPlayer(
            @PathVariable UUID sessionId,
            @PathVariable UUID playerId
    ) {
        var teams = addPlayerUseCase.execute(sessionId, playerId);
        List<SessionTeamResponse> response = sessionMapper.toTeamResponseList(teams);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/teams")
    public ResponseEntity<List<SessionTeamResponse>> getSessionTeams(@PathVariable UUID sessionId) {
        var teams = getTeamsUseCase.execute(sessionId);
        List<SessionTeamResponse> response = sessionMapper.toTeamResponseList(teams);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/players")
    public ResponseEntity<List<SessionActivePlayerResponse>> getSessionActivePlayers(@PathVariable UUID sessionId) {
        var activePlayers = getActivePlayersUseCase.execute(sessionId);
        List<SessionActivePlayerResponse> response = sessionMapper.toActivePlayerResponseList(activePlayers);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}/players/{playerId}")
    public ResponseEntity<List<SessionTeamResponse>> removePlayer(
            @PathVariable UUID sessionId,
            @PathVariable UUID playerId
    ) {
        var teams = removePlayerUseCase.execute(sessionId, playerId);
        List<SessionTeamResponse> response = sessionMapper.toTeamResponseList(teams);

      return ResponseEntity.ok(response);
    }

}
