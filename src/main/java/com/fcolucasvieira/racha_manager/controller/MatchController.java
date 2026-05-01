package com.fcolucasvieira.racha_manager.controller;

import com.fcolucasvieira.racha_manager.dto.FinishMatchRequest;
import com.fcolucasvieira.racha_manager.dto.MatchResponse;
import com.fcolucasvieira.racha_manager.dto.SessionTeamResponse;
import com.fcolucasvieira.racha_manager.mapper.SessionMapper;
import com.fcolucasvieira.racha_manager.usecase.FinishMatchUseCase;
import com.fcolucasvieira.racha_manager.usecase.GetCurrentMatchUseCase;
import com.fcolucasvieira.racha_manager.usecase.GetQueueUseCase;
import com.fcolucasvieira.racha_manager.usecase.StartQueueUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions/{sessionId}/matches")
@RequiredArgsConstructor
public class MatchController {
    private final StartQueueUseCase startQueueUseCase;
    private final GetCurrentMatchUseCase getCurrentMatchUseCase;
    private final GetQueueUseCase getQueueUseCase;
    private final FinishMatchUseCase finishMatchUseCase;
    private final SessionMapper mapper;

    @PostMapping("/start")
    public ResponseEntity<Void> startQueue(@PathVariable UUID sessionId) {
        startQueueUseCase.execute(sessionId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/current")
    public ResponseEntity<MatchResponse> getCurrentMatch(@PathVariable UUID sessionId){
        var match = getCurrentMatchUseCase.execute(sessionId);

        MatchResponse response = mapper.toMatchResponse(match);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/queue")
    public ResponseEntity<List<SessionTeamResponse>> getQueue(@PathVariable UUID sessionId){
        var queue = getQueueUseCase.execute(sessionId);

        List<SessionTeamResponse> response = mapper.toTeamResponseList(queue);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish")
    public ResponseEntity<Void> finishMatch(@PathVariable UUID sessionId, @RequestBody @Valid FinishMatchRequest request) {
        finishMatchUseCase.execute(sessionId, request.winnerTeamNumber());

        return ResponseEntity.ok().build();
    }
}
