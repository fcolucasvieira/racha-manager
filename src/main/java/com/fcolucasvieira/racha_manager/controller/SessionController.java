package com.fcolucasvieira.racha_manager.controller;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.dto.*;
import com.fcolucasvieira.racha_manager.mapper.SessionMapper;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
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
    private final CreateSessionUseCase createSessionUseCase;
    private final AddPlayerToSessionUseCase addPlayerToSessionUseCase;
    private final RemovePlayerFromSessionUseCase removePlayerFromSessionUseCase;
    private final FinishMatchUseCase finishMatchUseCase;

    private final SessionRepository sessionRepository;

    private final SessionMapper sessionMapper;


    @PostMapping
    public ResponseEntity<CreateSessionResponseDTO> createSession() {
        UUID id = createSessionUseCase.execute();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateSessionResponseDTO(id));
    }

    @PostMapping("/{sessionId}/players/{playerId}")
    public ResponseEntity<List<TeamDTO>> addPlayer(@PathVariable UUID sessionId,
                                                   @PathVariable UUID playerId) {
        List<Team> teams = addPlayerToSessionUseCase.execute(sessionId, playerId);

        return ResponseEntity.ok(
                teams.stream()
                        .map(sessionMapper::toTeamDTO)
                        .toList()
        );
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponseDTO> getSession(@PathVariable UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        return ResponseEntity.ok(sessionMapper.toResponse(session));
    }

    @DeleteMapping("/{sessionId}/players/{playerId}")
    public ResponseEntity<List<TeamDTO>> removePlayer(@PathVariable UUID sessionId,
                                                      @PathVariable UUID playerId) {
        List<Team> teams = removePlayerFromSessionUseCase.execute(sessionId, playerId);

        return ResponseEntity.ok(
                teams.stream()
                        .map(sessionMapper::toTeamDTO)
                        .toList()
        );
    }
}
