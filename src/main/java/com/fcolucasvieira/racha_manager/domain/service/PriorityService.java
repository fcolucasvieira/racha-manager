package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriorityService {

    private final TeamFillService teamFillService;

    private static final Logger log = LoggerFactory.getLogger(PriorityService.class);

    public void apply(Session session) {
        // se não existe currentMatch, retornar
        // retornar seria o ideal? Não seria ideal lançar uma excessão?
        if (!session.hasStarted()){
            log.debug(
                    "[PRIORITY_SKIPPED] sessionId={} reason=no_current_match",
                    session.getId()
            );

            return;
        }

        // instâncias de times do currentMatch (teamA, teamB)
        Team teamA = session.getCurrentMatch().getTeamA();
        Team teamB = session.getCurrentMatch().getTeamB();

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
