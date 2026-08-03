package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

// Domain Service anêmico
// Avaliar orquestração realocada em Session
@Service
@RequiredArgsConstructor
public class CurrentMatchRebalanceService {

    private final TeamFillService teamFillService;

    private static final Logger log = LoggerFactory.getLogger(CurrentMatchRebalanceService.class);

    public void apply(Session session) {
        // se não existe currentMatch, retornar
        if (!session.hasStarted()){
            log.debug(
                    "[PRIORITY_SKIPPED] sessionId={} reason=no_current_match",
                    session.getId()
            );

            return;
        }

        Match currentMatch = session.getCurrentMatch();

        // instâncias de times do currentMatch (teamA, teamB)
        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        // Waiting Queue substitui session.getQueue()
        // uso de .getQueue() para uma cópia da queue na session
        List<Team> queue = session.getQueue();

        // completar teamA
        teamFillService.fill(teamA, queue);
        // completar teamB
        teamFillService.fill(teamB, queue);

        // dissolver times vazios
        teamFillService.dissolveEmptyTeams(session);
    }
}
