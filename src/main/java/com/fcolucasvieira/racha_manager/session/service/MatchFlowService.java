package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchFlowService {
    // Por que MatchFlowService conhece TeamFillService?
    // MatchFlowService (Delegar APENAS o encerramento de partidas)
    private final TeamFillService teamFillService;

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

        // Quem deveria saber se a fila está vazia? (WaitingQueue)
        // Caso a queue esteja vazia, o próximo jogo é o mesmo que o anterior (vencedor vs perdedor)
        if(session.getQueue().isEmpty()) {
            session.setCurrentMatch(
                    new Match(winner, loser)
            );

            return;
        }

        // Quem deveria saber adicionar time a fila? (WaitingQueue)
        // derrotado vai para o final da fila
        session.addTeamToQueue(loser);

        // Quem deveria saber remover o time inicial da fila? (WaitingQueue)
        // seleta o primeiro time da queue
        Team next = session.removeFirstTeamFromQueue();

        // implementa o currentMatch entre vencedor e primeiro time da fila
        session.setCurrentMatch(
                new Match(winner, next)
        );
    }

    public void finishWithDraw(Session session) {
        // valida se há currentMatch e queue na sessão
        validateSessionState(session);

        // se existir menos que a qtde. de jogadores suficientes para 2 times na queue, lançar excessão
        if(!session.hasEnoughPlayersForDraw()) {
            throw new ConflictException("Cannot finish draw without enough players waiting");
        }

        // seleta currentMatch através da session
        Match currentMatch = session.getCurrentMatch();

        // seleta times da sessão através da variável currentMatch
        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        // marca os times que já jogaram
        teamA.markAsPlayed();
        teamB.markAsPlayed();

        // Quem deveria saber adicionar time a fila? (WaitingQueue)
        // joga os dois times para o final da fila (em ordem -> 1° A; 2º B)
        session.addTeamToQueue(teamA);
        session.addTeamToQueue(teamB);

        // Quem deveria saber remover time inicial da fila? (WaitingQueue)
        // seleta o primeiro time da fila para o currentMatch
        Team nextTeamA = session.removeFirstTeamFromQueue();

        // ajusta o primeiro time do novo currentMatch
        teamFillService.fill(nextTeamA, session.getQueue());

        // seleta o segundo time da fila para o currentMatch
        Team nextTeamB = session.removeFirstTeamFromQueue();

        // ajusta o segundo time do novo currentMatch
        teamFillService.fill(nextTeamB, session.getQueue());

        // atualiza currentMatch
        session.setCurrentMatch(
                new Match(nextTeamA, nextTeamB)
        );

        // dissolve times incompletos da sessão
        teamFillService.dissolveEmptyTeams(session);
    }

    private void validateSessionState(Session session) {
        if(!session.hasStarted()) {
            throw new ConflictException("No match in progress");
        }

        if(!session.hasQueue()) {
            throw new ConflictException("Queue not initialized");
        }
    }

    private void validateWinner(Team teamA,
                                Team teamB,
                                int winnerTeamNumber) {

        boolean invalidNumber =
                teamA.getNumber() != winnerTeamNumber &&
                teamB.getNumber() != winnerTeamNumber;

        if(invalidNumber) {
            throw new ValidationException("Invalid winner team number");
        }
    }
}
