package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriorityService {
    public void apply(List<Team> teams) {
        if(teams == null || teams.isEmpty()) return;

        for(int i = 0; i < teams.size() - 1; i++) {
            Team current = teams.get(i);

            if(current.isIncomplete()){
                fillTeam(current, teams, i);
            }
        }
    }

    private void fillTeam(Team target, List<Team> teams, int index) {
        for(int i = index + 1; i < teams.size(); i++) {
            Team donor = teams.get(i);

            while(target.isIncomplete() && !donor.getPlayers().isEmpty()) {
                target.addPlayer(donor.removeFirstPlayer());
            }

            if (target.isFull()){
                return;
            }
        }
    }
}
