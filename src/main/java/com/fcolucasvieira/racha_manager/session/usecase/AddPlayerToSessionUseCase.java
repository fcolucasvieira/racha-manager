package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.service.AddPlayerToActiveSessionService;
import com.fcolucasvieira.racha_manager.session.service.InitialTeamsBalancerService;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddPlayerToSessionUseCase {
    private final SessionRepository sessionRepository;
    private final PlayerRepository playerRepository;
    private final InitialTeamsBalancerService initialTeamsBalancerService;
    private final AddPlayerToActiveSessionService addPlayerToActiveSessionService;

    private static final Logger log = LoggerFactory.getLogger(AddPlayerToSessionUseCase.class);

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found with Id: " + sessionId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found with Id: " + playerId));

        synchronized (session) {
            session.addPlayer(player);

            log.info(
                    "[PLAYER_ADDED_TO_SESSION] sessionId={} playerId={} activePlayers={}",
                    sessionId,
                    playerId,
                    session.getActivePlayers().size()
            );

            if (session.canCreateInitialTeams()) {
                List<Team> teams = initialTeamsBalancerService.createInitialTeams(session);

                session.setTeams(teams);

                session.initializeSession();

                log.info(
                        "[SESSION_STARTED] sessionId={} currentMatch={}vs{} waitingTeams={}",
                        sessionId,
                        session.getCurrentMatch().getTeamA().getNumber(),
                        session.getCurrentMatch().getTeamB().getNumber(),
                        session.getWaitingQueue().getTeams().size()
                );
            } else if (session.hasStarted()) {
                addPlayerToActiveSessionService.addPlayer(session, player);

                log.info(
                        "[PLAYER_ADDED_TO_ACTIVE_SESSION] sessionId={} playerId={} totalTeams={} waitingTeams={}",
                        sessionId,
                        playerId,
                        session.getTeams().size(),
                        session.getWaitingQueue().getTeams().size()
                );
            }

            sessionRepository.save(session);

            return session.getTeams();
        }
    }
}
