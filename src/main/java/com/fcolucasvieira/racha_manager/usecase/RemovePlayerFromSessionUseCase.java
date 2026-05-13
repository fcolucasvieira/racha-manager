package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.InitialTeamBalancerService;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemovePlayerFromSessionUseCase {

    private final SessionRepository sessionRepository;
    private final PriorityService priorityService;

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        // encontra o time do jogador
        Team team = findPlayerTeam(session, playerId);

        // remove da sessão -> remove de activePlayers
        session.removePlayer(playerId);

        // remove do time
        team.removePlayerById(playerId);

        // dissolve se vazio
        if(team.getPlayers().isEmpty()) {
            session.removeTeam(team);
        }

        // reorganiza currentMatch
        if(session.hasStarted()) {
            priorityService.apply(session);
        }

        sessionRepository.save(session);

        return session.getTeams();
    }

    private Team findPlayerTeam(Session session, UUID playerId) {
        return session.getTeams().stream()
                .filter(team -> team.getPlayers().stream()
                        .anyMatch(player ->
                                player.getId().equals(playerId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player is not in a team"));
    }
}
