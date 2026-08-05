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
        validateInitialShuffle(session);

        List<Player> players = new ArrayList<>(
                session.getActivePlayers()
        );

        Collections.shuffle(players);

        Team t1 = new Team(1);
        Team t2 = new Team(2);

        List<Team> teams = List.of(t1, t2);

        for(int i = 0; i < INITIAL_PLAYERS; i++) {
            teams.get(i / TEAM_SIZE)
                    .addPlayer(players.get(i));
        }

        session.markAsShuffled();

        log.info(
                "[INITIAL_BALANCE_COMPLETED] sessionId={} teams={} players={}",
                session.getId(),
                teams.size(),
                session.getActivePlayers().size()
        );

        return teams;
    }

    private void validateInitialShuffle(Session session) {
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