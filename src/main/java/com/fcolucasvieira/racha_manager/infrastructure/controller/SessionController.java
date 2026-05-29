package com.fcolucasvieira.racha_manager.infrastructure.controller;

import com.fcolucasvieira.racha_manager.application.dto.CreateSessionResponse;
import com.fcolucasvieira.racha_manager.application.dto.FinishMatchRequest;
import com.fcolucasvieira.racha_manager.application.dto.SessionResponseDTO;
import com.fcolucasvieira.racha_manager.application.dto.TeamDTO;
import com.fcolucasvieira.racha_manager.application.usecase.*;
import com.fcolucasvieira.racha_manager.domain.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.application.mapper.SessionMapper;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
import com.fcolucasvieira.racha_manager.infrastructure.response.ApiResponse;
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
    private final GetSessionUseCase getSessionUseCase;

    private final SessionMapper sessionMapper;


    @PostMapping
    public ResponseEntity<ApiResponse<CreateSessionResponse>> createSession() {
        UUID id = createSessionUseCase.execute();

        CreateSessionResponse response = new CreateSessionResponse(id);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(response, "Session created successfully")
                );
    }

    @PostMapping("/{sessionId}/players/{playerId}")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> addPlayer(@PathVariable UUID sessionId,
                                                   @PathVariable UUID playerId) {
        List<Team> teams = addPlayerToSessionUseCase.execute(sessionId, playerId);

        List<TeamDTO> response = teams.stream()
                .map(sessionMapper::toTeamDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(response, "Player added successfully")
        );
    }

    @PostMapping("/{sessionId}/finish-match")
    public ResponseEntity<ApiResponse<Void>> finishMatch(@PathVariable UUID sessionId,
                                            @RequestBody @Valid FinishMatchRequest request) {
        finishMatchUseCase.execute(
                sessionId,
                request.winnerTeamNumber(),
                request.resultType()
        );

        return ResponseEntity.ok(
                ApiResponse.success(null, "Match finished successfully")
        );
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> getSession(@PathVariable UUID sessionId) {
        Session session = getSessionUseCase.execute(sessionId);

        SessionResponseDTO response = sessionMapper.toResponse(session);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Session retrieved successfully")
        );
    }

    @DeleteMapping("/{sessionId}/players/{playerId}")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> removePlayer(@PathVariable UUID sessionId,
                                                      @PathVariable UUID playerId) {
        List<Team> teams = removePlayerFromSessionUseCase.execute(sessionId, playerId);

        List<TeamDTO> response = teams.stream()
                .map(sessionMapper::toTeamDTO)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(response, "Player removed successfully")
        );
    }
}
