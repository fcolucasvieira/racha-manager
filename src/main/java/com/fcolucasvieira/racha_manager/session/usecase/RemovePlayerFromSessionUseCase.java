package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.service.TeamCompletionService;
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
    private final TeamCompletionService teamCompletionService;

    private static final Logger log = LoggerFactory.getLogger(RemovePlayerFromSessionUseCase.class);

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found with Id: " + sessionId));

        Team playerTeam = session.findPlayerTeam(playerId);

        boolean playerTeamWasCurrentMatch = session.isCurrentMatchTeam(playerTeam);

        session.removePlayer(playerId);

        if(playerTeamWasCurrentMatch)
            teamCompletionService.complete(playerTeam, session.getWaitingQueue());

        log.info(
                "[PLAYER_REMOVED] sessionId={} playerId={} activePlayers={}",
                sessionId,
                playerId,
                session.getActivePlayers().size()
        );

        sessionRepository.save(session);

        return session.getTeams();
    }
}
