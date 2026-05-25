package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriorityService {

    private static final Logger log = LoggerFactory.getLogger(PriorityService.class);

    public void apply(Session session) {
        // se não existe currentMatch, retornar
        if (session.getCurrentMatch() == null){
            return;
        }

        // instâncias de times do currentMatch (teamA, teamB)
        Team teamA = session.getCurrentMatch().getTeamA();
        Team teamB = session.getCurrentMatch().getTeamB();

        // uso de .getQueue() para uma cópia da queue na session
        List<Team> queue = session.getQueue();

        // completar teamA
        fill(teamA, queue);
        // completar teamB
        fill(teamB, queue);

        // dissolver times vazios
        dissolveEmptyTeams(session);
    }

    private void fill(Team target, List<Team> queue) {
        // loop entre times da queue (em ordem)
        for (Team donor : queue) {
            // loop para adição de jogadores ao time alvo enquanto estiver incompleto
            while (target.isIncomplete() && !donor.getPlayers().isEmpty()) {

                var transferredPlayer = donor.removeFirstPlayer();

                target.addPlayer(transferredPlayer);

                /*
                log.info(
                        "[PLAYER_TRANSFERRED] donorTeam={} targetTeam={} playerId={}",
                        donor.getNumber(),
                        target.getNumber(),
                        transferredPlayer.getId()
                );
                 */
            }

            // se completo, retornar
            if (target.isFull()) {
                return;
            }
        }
    }

    private void dissolveEmptyTeams(Session session) {
        List<Team> teamsToRemove = session.getTeams().stream()
                .filter(team -> team.getPlayers().isEmpty())
                .toList();

        for(Team team : teamsToRemove) {
            session.removeTeam(team);

            /*
            log.info(
                    "[EMPTY_TEAM_DISSOLVED] sessionId={} teamNumber={}",
                    session.getId(),
                    team.getNumber()
            );

             */
        }
    }
}
