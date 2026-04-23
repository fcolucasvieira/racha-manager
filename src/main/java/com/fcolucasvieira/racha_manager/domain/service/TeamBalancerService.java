package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TeamBalancerService {

    private static final int TEAM_SIZE = 4;

    public List<Team> balance(Session session) {

        List<PlayerEntity> players = new ArrayList<>(session.getActivePlayers());

        if(session.getTeams().isEmpty()) {
            Collections.shuffle(players);
        }

        List<Team> teams = new ArrayList<>();
        int teamNumber = 1;

        for(int i = 0; i < players.size(); i += TEAM_SIZE){
            Team team = new Team(teamNumber++);

            for(int j = i; j < i + TEAM_SIZE && j < players.size(); j++){
                team.addPlayer(players.get(j));
            }

            teams.add(team);
        }
        return teams;
    }
}
