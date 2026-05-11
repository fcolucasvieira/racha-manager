package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.domain.service.InitialTeamBalancerService;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
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

    public List<Team> execute(UUID sessionId, UUID playerId) {
        Session session = sessionRepository.findById(sessionId)
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        PlayerEntity player = playerRepository.findById(playerId)
                        .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        session.addPlayer(player);

        if(shouldCreateInitialTeams(session)){
            List<Team> teams = initialTeamBalancerService.createInitialTeams(session);

            session.updateTeams(teams);

            sessionRepository.save(session);

            return teams;
        }

        if (session.hasStarted()) {
            addPlayerIncremental(session, player);

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
