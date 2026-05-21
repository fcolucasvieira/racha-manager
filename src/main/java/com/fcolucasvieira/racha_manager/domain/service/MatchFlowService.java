package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.springframework.stereotype.Service;

@Service
public class MatchFlowService {
    public void finishWithWinner(Session session, int winnerTeamNumber) {
        // valida se há currentMatch e queue na sessão
        validateSessionState(session);

        Match currentMatch = session.getCurrentMatch();

        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        // valida se winnerTeamNumber está entre times do currentMatch
        validateWinner(teamA, teamB, winnerTeamNumber);

        // seleta o vencedor
        Team winner = teamA.getNumber() == winnerTeamNumber
                ? teamA
                : teamB;

        // seleta o perdedor
        Team loser = currentMatch.getLoser(winner);

        // Caso a queue esteja vazia, o próximo jogo é o mesmo que o anterior (vencedor vs perdedor)
        if(session.getQueue().isEmpty()) {
            session.updateCurrentMatch(
                    new Match(winner, loser)
            );

            return;
        }

        // derrotado vai para o final da fila
        session.addTeamToQueue(loser);

        // seleta o primeiro time da queue
        Team next = session.removeFirstTeamFromQueue();

        // implementa o currentMatch entre vencedor e primeiro time da fila
        session.updateCurrentMatch(
                new Match(winner, next)
        );
    }

    private void validateSessionState(Session session) {
        if(!session.hasStarted()) {
            throw new IllegalStateException("No match in progress");
        }

        if(!session.hasQueue()) {
            throw new IllegalStateException("Queue not initialized");
        }
    }

    private void validateWinner(Team teamA,
                                Team teamB,
                                int winnerTeamNumber) {

        boolean invalidNumber =
                teamA.getNumber() != winnerTeamNumber &&
                teamB.getNumber() != winnerTeamNumber;

        if(invalidNumber) {
            throw new IllegalArgumentException("Invalid winner team number");
        }
    }
}
