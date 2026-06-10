package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.service.PriorityService;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemovePlayerFromSessionUseCase {

    private final SessionRepository sessionRepository;
    private final PriorityService priorityService;

    private static final Logger log = LoggerFactory.getLogger(RemovePlayerFromSessionUseCase.class);

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

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

        sessionRepository.save(session);

        return session.getTeams();
    }
}
