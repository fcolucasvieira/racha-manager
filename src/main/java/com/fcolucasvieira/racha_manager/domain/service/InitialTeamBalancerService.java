package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.exception.ConflictException;
import com.fcolucasvieira.racha_manager.domain.exception.ValidationException;
import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class InitialTeamBalancerService {

    private static final int TEAM_SIZE = 4;
    private static final int INITIAL_PLAYERS = 8;

    private static final Logger log = LoggerFactory.getLogger(InitialTeamBalancerService.class);

    public List<Team> createInitialTeams(Session session) {

        validateInitialBalance(session);

        // Lista baseada nos jogadores ativos da sessão
        List<PlayerEntity> players =
                new ArrayList<>(session.getActivePlayers());

        // Essa lista sofre balanceamento de jogadores
        Collections.shuffle(players);

        // Definimos que a sessão já foi balanceada (útil para evitar novos balanceamentos em caso de retomada a 8 jogadores)
        session.markAsShuffled();

        // Lista de times gerada para adição de jogadores já balanceados
        List<Team> teams = new ArrayList<>();

        Team team1 = new Team(1);
        Team team2 = new Team(2);

        for (int i = 0; i < TEAM_SIZE; i++) {
            team1.addPlayer(players.get(i));
        }

        for (int i = TEAM_SIZE; i < INITIAL_PLAYERS; i++) {
            team2.addPlayer(players.get(i));
        }

        teams.add(team1);
        teams.add(team2);

        log.info(
                "[INITIAL_BALANCE_COMPLETED] sessionId={} teams={} players={}",
                session.getId(),
                teams.size(),
                session.getActivePlayers().size()
        );

        return teams;
    }

    private void validateInitialBalance(Session session) {
        if (session == null) {
            throw new ValidationException("Session cannot be null");
        }

        if (session.isShuffled()) {
            throw new ConflictException("Initial shuffle already performed");
        }

        if (session.getActivePlayers().size() != INITIAL_PLAYERS) {
            throw new ConflictException("Initial balance requires exactly 8 players");
        }

        if (session.getTeams() != null && !session.getTeams().isEmpty()) {
            throw new ConflictException("Session already contains teams");
        }
    }
}