package com.fcolucasvieira.racha_manager.infrastructure.controller;

import com.fcolucasvieira.racha_manager.application.dto.CreateSessionResponse;
import com.fcolucasvieira.racha_manager.application.dto.FinishMatchRequest;
import com.fcolucasvieira.racha_manager.application.dto.SessionResponseDTO;
import com.fcolucasvieira.racha_manager.application.dto.TeamDTO;
import com.fcolucasvieira.racha_manager.application.usecase.AddPlayerToSessionUseCase;
import com.fcolucasvieira.racha_manager.application.usecase.CreateSessionUseCase;
import com.fcolucasvieira.racha_manager.application.usecase.FinishMatchUseCase;
import com.fcolucasvieira.racha_manager.application.usecase.RemovePlayerFromSessionUseCase;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.application.mapper.SessionMapper;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
import jakarta.validation.Valid;
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

    private final SessionRepositoryPort sessionRepositoryPort;

    private final SessionMapper sessionMapper;


    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession() {
        UUID id = createSessionUseCase.execute();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateSessionResponse(id));
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

    @PostMapping("/{sessionId}/finish-match")
    public ResponseEntity<Void> finishMatch(@PathVariable UUID sessionId,
                                            @RequestBody @Valid FinishMatchRequest request) {
        finishMatchUseCase.execute(
                sessionId,
                request.winnerTeamNumber(),
                request.resultType()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponseDTO> getSession(@PathVariable UUID sessionId) {
        Session session = sessionRepositoryPort.findById(sessionId)
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
