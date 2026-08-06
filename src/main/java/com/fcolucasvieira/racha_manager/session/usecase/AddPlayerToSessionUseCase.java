package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.service.InitialTeamsBalancerService;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.constant.RachaRules.INITIAL_PLAYERS;

@Service
@RequiredArgsConstructor
public class AddPlayerToSessionUseCase {
    private final SessionRepository sessionRepository;
    private final PlayerRepository playerRepository;
    private final InitialTeamsBalancerService initialTeamsBalancerService;

    private static final Logger log = LoggerFactory.getLogger(AddPlayerToSessionUseCase.class);

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found with Id: " + sessionId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found with Id: " + playerId));

        session.addPlayer(player);

        log.info(
                "[PLAYER_ADDED] sessionId={} playerId={} activePlayers={}",
                sessionId,
                playerId,
                session.getActivePlayers().size()
        );

        if (session.canStartInitialShuffle()) {
            List<Team> teams = initialTeamsBalancerService.createInitialTeams(session);

            session.setTeams(teams);

            session.initializeSession();

            log.info(
                    "[QUEUE_STARTED] sessionId={} currentMatch={}vs{} queueTeams={}",
                    sessionId,
                    session.getCurrentMatch().getTeamA().getNumber(),
                    session.getCurrentMatch().getTeamB().getNumber(),
                    session.getWaitingTeams().size()
            );
        } else if (session.hasStarted()) {
            addPlayerToRunningSession(session, player);

            log.info(
                    "[PLAYER_ADDED_TO_RUNNING_SESSION] sessionId={} playerId={} totalTeams={}",
                    sessionId,
                    playerId,
                    session.getTeams().size()
            );
        }

        sessionRepository.save(session);

        return session.getTeams();
    }

    // Domain Service (Sprint de refatoração arquitetural)
    private void addPlayerToRunningSession(Session session, Player player) {
        // Instancia teams da session
        List<Team> teams = session.getTeams();

        // Busca o último time da lista
        Team lastTeam = teams.isEmpty()
                ? null
                : teams.getLast();

        // Se não existe time ou último está cheio
        if (lastTeam == null || lastTeam.isFull()) {
            // Define número do novo time
            int nextTeamNumber =
                    (teams.stream()
                    .mapToInt(Team::getNumber)
                    .max()
                    .orElse(0))
                    + 1;

            // Cria novo time
            Team newTeam = new Team(nextTeamNumber);

            // Adiciona o jogador ao time
            newTeam.addPlayer(player);

            // Adiciona o time (com o jogador) na lista de times
            teams.add(newTeam);

            // Adiciona time a fila de prioridade (no final)
            session.getWaitingQueue().add(newTeam);

            return;
        }

        // Adiciona no último incompleto
        lastTeam.addPlayer(player);
    }
}
