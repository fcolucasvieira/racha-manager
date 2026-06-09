package com.fcolucasvieira.racha_manager.session.controller;

import com.fcolucasvieira.racha_manager.session.dto.response.CreateSessionResponse;
import com.fcolucasvieira.racha_manager.session.dto.request.FinishMatchRequest;
import com.fcolucasvieira.racha_manager.session.dto.response.SessionDTO;
import com.fcolucasvieira.racha_manager.session.dto.response.TeamDTO;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.mapper.SessionMapper;
import com.fcolucasvieira.racha_manager.common.response.ApiResponse;
import com.fcolucasvieira.racha_manager.session.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Sessions",
        description = "Operations related to racha sessions and matches"
)
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

    @Operation(
            summary = "Create a new session",
            description = "Creates a new racha session and returns its identifier."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSessionResponse>> createSession() {
        UUID id = createSessionUseCase.execute();

        CreateSessionResponse response = new CreateSessionResponse(id);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(response, "Session created successfully")
                );
    }

    @Operation(
            summary = "Add player to session",
            description = """
            Adds an existing player to a session.

            When the eighth player joins, the system automatically:
            - Creates the first two teams
            - Balances players randomly
            - Starts the match queue
            """
    )
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

    @Operation(
            summary = "Finish current match",
            description = """
            Finishes the current match.

            Supported results:
            - WINNER: a winning team must be provided
            - DRAW: winnerTeamNumber must be null

            The queue and priorities are automatically updated.
            """
    )
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

    @Operation(
            summary = "Get session details",
            description = """
            Retrieves the current state of a session, including:

            - Current match
            - Teams in queue
            - Session status
            """
    )
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<SessionDTO>> getSession(@PathVariable UUID sessionId) {
        Session session = getSessionUseCase.execute(sessionId);

        SessionDTO response = sessionMapper.toResponse(session);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Session retrieved successfully")
        );
    }

    @Operation(
            summary = "Remove player from session",
            description = """
            Removes a player from a session.

            Empty teams may be dissolved automatically and
            priorities can be recalculated when necessary.
            """
    )
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
