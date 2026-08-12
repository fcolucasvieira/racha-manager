package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.springframework.stereotype.Service;

@Service
public class AddPlayerToActiveSessionService {
    public void addPlayer(Session session, Player player) {
        Team lastTeam = session.getLastTeam();

        if(lastTeam == null || lastTeam.isFull()) {
            Team nextTeam = session.createNextTeam();

            nextTeam.addPlayer(player);

            session.addTeam(nextTeam);

            return;
        }

        lastTeam.addPlayer(player);
    }
}
