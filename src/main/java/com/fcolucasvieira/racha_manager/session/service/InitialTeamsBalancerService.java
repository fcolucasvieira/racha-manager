package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fcolucasvieira.racha_manager.session.rules.RachaRules.INITIAL_PLAYERS;
import static com.fcolucasvieira.racha_manager.session.rules.RachaRules.TEAM_SIZE;

// (Futuro) Generalizar algoritmo para N times e M jogadores por time
@Service
public class InitialTeamsBalancerService {
    private static final Logger log = LoggerFactory.getLogger(InitialTeamsBalancerService.class);

    public List<Team> createInitialTeams(Session session) {
        List<Player> players = new ArrayList<>(
                session.getActivePlayers()
        );

        Collections.shuffle(players);

        List<Team> teams = createTeams();

        distributePlayers(players, teams);

        session.markInitialTeamsAsCreated();

        log.info(
                "[INITIAL_BALANCE_COMPLETED] sessionId={} teams={} players={}",
                session.getId(),
                teams.size(),
                session.getActivePlayers().size()
        );

        return teams;
    }

    private List<Team> createTeams() {
        Team teamA = new Team(1);
        Team teamB = new Team(2);

        return new ArrayList<>(
                List.of(teamA, teamB)
        );
    }

    private void distributePlayers(List<Player> players, List<Team> teams) {
        for(int i = 0; i < INITIAL_PLAYERS; i++) {
            Team team = teams.get(i / TEAM_SIZE);
            team.addPlayer(players.get(i));
        }
    }
}