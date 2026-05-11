package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriorityService {

    public void apply(Session session) {

        if (session.getCurrentMatch() == null) return;

        Team teamA = session.getCurrentMatch().getTeamA();
        Team teamB = session.getCurrentMatch().getTeamB();

        List<Team> queue = session.getQueue();

        fill(teamA, queue);
        fill(teamB, queue);
    }

    private void fill(Team target, List<Team> queue) {

        for (Team donor : queue) {

            while (target.isIncomplete() && !donor.getPlayers().isEmpty()) {
                target.addPlayer(donor.removeFirstPlayer());
            }

            if (target.isFull()) return;
        }
    }
}
