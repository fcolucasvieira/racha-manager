package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.service.TeamBalancerService;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddToPlayerInSessionUseCase {
    private final SessionRepository sessionRepository;
    private final PlayerRepository playerRepository;
    private final TeamBalancerService teamBalancerService;


    public void execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        session.addPlayer(player);

        var teams = teamBalancerService.balance(session);

        session.updateTeams(teams);

        sessionRepository.save(session);
    }
}
