package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamFillService {
    private static final Logger log = LoggerFactory.getLogger(TeamFillService.class);

    public void fill(Team target, List<Team> queue) {
        // loop entre times da queue (em ordem)
        for (Team donor : queue) {
            // loop para adição de jogadores ao time alvo enquanto estiver incompleto
            while (target.isIncomplete() && !donor.getPlayers().isEmpty()) {

                var transferredPlayer = donor.removeFirstPlayer();

                target.addPlayer(transferredPlayer);

                log.info(
                        "[PLAYER_TRANSFERRED] donorTeam={} targetTeam={} playerId={}",
                        donor.getNumber(),
                        target.getNumber(),
                        transferredPlayer.getId()
                );
            }

            // se completo, retornar
            if (target.isFull()) {
                return;
            }
        }
    }

    public void dissolveEmptyTeams(Session session) {
        List<Team> teamsToRemove = session.getTeams().stream()
                .filter(team ->
                        team.getPlayers().isEmpty() &&
                                !session.isCurrentMatchTeam(team)
                )
                .toList();

        for(Team team : teamsToRemove) {
            session.removeTeam(team);

            log.info(
                    "[EMPTY_TEAM_DISSOLVED] sessionId={} teamNumber={}",
                    session.getId(),
                    team.getNumber()
            );
        }
    }
}
