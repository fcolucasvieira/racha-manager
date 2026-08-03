package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fcolucasvieira.racha_manager.session.constant.RachaRules.INITIAL_PLAYERS;
import static com.fcolucasvieira.racha_manager.session.constant.RachaRules.TEAM_SIZE;

// (Futuro) Generalizar algoritmo para N times e M jogadores por time
@Service
public class InitialTeamsBalancerService {

    private static final Logger log = LoggerFactory.getLogger(InitialTeamsBalancerService.class);

    public List<Team> createInitialTeams(Session session) {

        validateInitialBalance(session);

        // Lista baseada nos jogadores ativos da sessão
        List<Player> players =
                new ArrayList<>(session.getActivePlayers());

        // Essa lista sofre balanceamento de jogadores
        Collections.shuffle(players);

        // Lista de times gerada para adição de jogadores já balanceados
        List<Team> teams = new ArrayList<>();

        // (Futuro) Gerar TeamFactory?
        Team t1 = new Team(1);
        Team t2 = new Team(2);

        for (int i = 0; i < TEAM_SIZE; i++) {
            t1.addPlayer(players.get(i));
        }

        for (int i = TEAM_SIZE; i < INITIAL_PLAYERS; i++) {
            t2.addPlayer(players.get(i));
        }

        teams.add(t1);
        teams.add(t2);

        // nome de método melhor? (nome + descritivo)
        // Definimos que a sessão já foi balanceada (evita novos balanceamentos em caso de retomada a qtde. de INITIAL_PLAYERS)
        session.markAsShuffled();

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
            throw new ConflictException("Initial balance requires exactly " + INITIAL_PLAYERS + " players");
        }

        if (session.getTeams() != null && !session.getTeams().isEmpty()) {
            throw new ConflictException("Session already contains teams");
        }
    }
}