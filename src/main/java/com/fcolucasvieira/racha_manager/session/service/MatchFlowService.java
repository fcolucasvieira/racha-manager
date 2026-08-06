package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchFlowService {
    // Por que MatchFlowService conhece TeamCompletionService?
    // MatchFlowService (Delegar APENAS o encerramento de partidas)
    private final TeamCompletionService teamCompletionService;

    public void finishWithWinner(Session session, int winnerTeamNumber) {
        validateSessionState(session);

        Match currentMatch = session.getCurrentMatch();

        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        validateWinnerNumber(teamA, teamB, winnerTeamNumber);

        Team winner = teamA.getNumber() == winnerTeamNumber
                ? teamA
                : teamB;

        Team loser = currentMatch.getLoser(winner);

        winner.markAsPlayed();
        loser.markAsPlayed();

        WaitingQueue waitingQueue = session.getWaitingQueue();

        if(waitingQueue.isEmpty()) {
            session.setCurrentMatch(
                    new Match(winner, loser)
            );

            return;
        }

        waitingQueue.add(loser);

        Team next = waitingQueue.poll();
        teamCompletionService.complete(next, waitingQueue);

        session.setCurrentMatch(
                new Match(winner, next)
        );
    }

    public void finishWithDraw(Session session) {
        validateSessionState(session);

        WaitingQueue waitingQueue = session.getWaitingQueue();

        if(!waitingQueue.hasEnoughForDraw())
            throw new ConflictException("Cannot finish draw without enough players waiting");

        Match currentMatch = session.getCurrentMatch();

        Team teamA = currentMatch.getTeamA();
        Team teamB = currentMatch.getTeamB();

        teamA.markAsPlayed();
        teamB.markAsPlayed();

        waitingQueue.add(teamA);
        waitingQueue.add(teamB);

        Team nextTeamA = waitingQueue.poll();
        teamCompletionService.complete(nextTeamA, waitingQueue);

        Team nextTeamB = waitingQueue.poll();
        teamCompletionService.complete(nextTeamB, waitingQueue);

        session.setCurrentMatch(
                new Match(nextTeamA, nextTeamB)
        );

        session.removeEmptyTeams();
    }

    private void validateSessionState(Session session) {
        if(!session.hasStarted())
            throw new ConflictException("No match in progress");
    }

    private void validateWinnerNumber(Team tA, Team tB, int winnerTeamNumber) {
        boolean invalidNumber =
                tA.getNumber() != winnerTeamNumber &&
                tB.getNumber() != winnerTeamNumber;

        if(invalidNumber)
            throw new ValidationException("Invalid winner team number");
    }
}
