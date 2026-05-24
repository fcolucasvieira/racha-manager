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

        // instância currentMatch e seus times
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

        // marca times que já jogaram
        winner.markAsPlayed();
        loser.markAsPlayed();

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

    public void finishWithDraw(Session session) {
        // valida se há currentMatch e queue na sessão
        validateSessionState(session);

        // se existir menos que 2 times na queue, lançar excessão
        if(!session.hasAtLeastTeamsInQueue(2)) {
            throw new IllegalStateException("Cannot finish draw without at least 2 teams in queue");
        }

        // seleta currentMatch através da session
        Match currentMatch = session.getCurrentMatch();

        // seleta times da sessão através da variável currentMatch
        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        // joga os dois times para o final da fila (em ordem -> 1° A; 2º B)
        session.addTeamToQueue(teamA);
        session.addTeamToQueue(teamB);

        // marca os times que já jogaram
        teamA.markAsPlayed();
        teamB.markAsPlayed();

        // seleta os dois primeiros times da fila para o currentMatch
        Team nextTeamA = session.removeFirstTeamFromQueue();
        Team nextTeamB = session.removeFirstTeamFromQueue();

        // atualiza currentMatch
        session.updateCurrentMatch(
                new Match(nextTeamA, nextTeamB)
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
