package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.domain.service.InitialTeamBalancerService;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
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
    private final InitialTeamBalancerService initialTeamBalancerService;
    private final PriorityService priorityService;

    private static final Logger log = LoggerFactory.getLogger(AddPlayerToSessionUseCase.class);

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        PlayerEntity player = playerRepository.findById(playerId)
                        .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        session.addPlayer(player);

        log.info(
                "[PLAYER_ADDED] sessionId={} playerId={} activePlayers={}",
                sessionId,
                playerId,
                session.getActivePlayers().size()
        );

        if(shouldCreateInitialTeams(session)){
            List<Team> teams = initialTeamBalancerService.createInitialTeams(session);

            session.updateTeams(teams);

            session.startQueue();

            log.info(
                    "[QUEUE_STARTED] sessionId={} currentMatch={}vs{} queueSize={}",
                    sessionId,
                    session.getCurrentMatch().getTeamA().getNumber(),
                    session.getCurrentMatch().getTeamB().getNumber(),
                    session.getQueue().size()
            );

            sessionRepository.save(session);

            return session.getTeams();
        }

        if (session.hasStarted()) {
            addPlayerIncremental(session, player);

            log.info(
                    "[INCREMENTAL_PLAYER_ADDED] sessionId={} playerId={} totalTeams={}",
                    sessionId,
                    playerId,
                    session.getTeams().size()
            );

            priorityService.apply(session);
        }

        sessionRepository.save(session);

        return session.getTeams();
    }

    private boolean shouldCreateInitialTeams(Session session) {
        return session.getActivePlayers().size() == 8 &&
                !session.isShuffled();
    }

    private void addPlayerIncremental(Session session, PlayerEntity player) {
        // Instancia teams da session
        List<Team> teams = session.getTeams();

        // Busca o último time da lista
        Team lastTeam = teams.isEmpty()
                ? null
                : teams.get(teams.size() - 1);

        // Se não existe time ou último está cheio
        if (lastTeam == null || lastTeam.isFull()) {

            // Cria novo time
            Team newTeam = new Team(teams.size() + 1);

            // Adiciona o jogador ao time
            newTeam.addPlayer(player);

            // Adiciona o time (com o jogador) na lista de times
            teams.add(newTeam);

            // Se fila já iniciou
            if (session.hasStarted()) {
                // Adiciona time a fila de prioridade (no final)
                session.addTeamToQueue(newTeam);
            }

            return;
        }

        // Adiciona no último incompleto
        lastTeam.addPlayer(player);
    }
}
