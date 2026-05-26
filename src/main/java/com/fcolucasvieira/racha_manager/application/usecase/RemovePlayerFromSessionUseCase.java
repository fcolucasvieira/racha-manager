package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemovePlayerFromSessionUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final PriorityService priorityService;

    private static final Logger log = LoggerFactory.getLogger(RemovePlayerFromSessionUseCase.class);

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        // encontra o time do jogador
        Team team = session.findPlayerTeam(playerId);

        // remove do activePlayers
        session.removePlayer(playerId);

        log.info(
                "[PLAYER_REMOVED] sessionId={} playerId={} activePlayers={}",
                sessionId,
                playerId,
                session.getActivePlayers().size()
        );

        // remove do time
        team.removePlayerById(playerId);

        // dissolve time se vazio
        if(team.getPlayers().isEmpty()) {
            session.validateTeamRemoval(team);

            session.removeTeam(team);

            log.info(
                    "[TEAM_DISSOLVED] sessionId={} teamNumber={}",
                    sessionId,
                    team.getNumber()
            );
        }

        // reorganiza prioridades do currentMatch
        if(session.isCurrentMatchTeam(team)) {
            priorityService.apply(session);

            log.info(
                    "[PRIORITY_APPLIED] sessionId={}",
                    sessionId
            );
        }

        sessionRepositoryPort.save(session);

        return session.getTeams();
    }
}
