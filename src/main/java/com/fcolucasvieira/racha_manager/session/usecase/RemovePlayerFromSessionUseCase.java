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

        // encontra time do jogador
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
        if(!team.hasPlayers()) {
            session.removeTeam(team);

            log.info(
                    "[TEAM_DISSOLVED] sessionId={} teamNumber={}",
                    sessionId,
                    team.getNumber()
            );
        }

        // reorganiza time em partida atual, caso jogador removido estivesse no time
        if(session.isCurrentMatchTeam(team))
            completeCurrentMatchTeams(session);

        sessionRepository.save(session);

        return session.getTeams();
    }

    private void completeCurrentMatchTeams(Session session) {
        Match currentMatch = session.getCurrentMatch();

        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        WaitingQueue waitingQueue = session.getWaitingQueue();

        teamCompletionService.complete(teamA, waitingQueue);
        teamCompletionService.complete(teamB, waitingQueue);

        log.info("[CURRENT_MATCH_TEAMS_COMPLETED] sessionId={} teamAPlayers={} teamBPlayers={}",
                session.getId(),
                teamA.getPlayersCount(),
                teamB.getPlayersCount()
        );

        session.removeEmptyTeams();
    }
}
