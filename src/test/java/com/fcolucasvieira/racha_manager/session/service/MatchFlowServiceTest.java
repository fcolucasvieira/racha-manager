package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchFlowServiceTest {
    @Mock
    private TeamCompletionService teamCompletionService;

    @InjectMocks
    private MatchFlowService matchFlowService;

    private Team createTeam(int number, int countPlayers) {
        Team team = new Team(number);

        for (int i = 1; i <= countPlayers; i++) {
            team.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }
        return team;
    }

    @Test
    @DisplayName("Should finish match with winner when teams are waiting")
    void shouldFinishMatchWithWinnerSuccessfullyWithTeamsInQueue() {
        Session session = new Session();

        Team teamA = createTeam(1, 4);
        Team teamB = createTeam(2, 4);
        Team waitingTeam = createTeam(3, 2);

        session.setTeams(
                new ArrayList<>(List.of(teamA, teamB, waitingTeam))
        );

        session.initializeSession();

        matchFlowService.finishWithWinner(session, 1);

        Match currentMatch = session.getCurrentMatch();

        assertEquals(1, currentMatch.getTeamA().getNumber());
        assertEquals(3, currentMatch.getTeamB().getNumber());

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(1, waitingQueue.teams().size());
        assertEquals(2, waitingQueue.teams().getFirst().getNumber());

        assertTrue(teamA.isPlayed());
        assertTrue(teamB.isPlayed());

        verify(teamCompletionService).complete(waitingTeam, waitingQueue);
    }

    @Test
    @DisplayName("Should finish match with winner when waiting queue is empty")
    void shouldFinishMatchWithWinnerSuccessfullyWithoutTeamsInQueue() {
        Session session = new Session();

        Team teamA = createTeam(1, 4);
        Team teamB = createTeam(2, 4);

        session.setTeams(
                new ArrayList<>(List.of(teamA, teamB))
        );

        session.initializeSession();

        matchFlowService.finishWithWinner(session, 1);

        Match currentMatch = session.getCurrentMatch();

        assertEquals(1, currentMatch.getTeamA().getNumber());
        assertEquals(2, currentMatch.getTeamB().getNumber());

        assertTrue(session.getWaitingQueue().isEmpty());

        assertTrue(teamA.isPlayed());
        assertTrue(teamB.isPlayed());

        verify(teamCompletionService, never()).complete(any(), any());

    }

    @Test
    @DisplayName("Should throw exception when session has no match in progress")
    void shouldThrowExceptionWhenNoMatchInProgress() {
        Session session = new Session();

        assertThrows(
                ConflictException.class,
                () -> matchFlowService.finishWithWinner(session, 1)
        );
    }

    @Test
    @DisplayName("Should throw exception when winner team number is invalid")
    void shouldThrowExceptionWhenWinnerIsInvalid() {
        Session session = new Session();

        Team teamA = createTeam(1, 4);
        Team teamB = createTeam(2, 4);

        session.setTeams(
                new ArrayList<>(List.of(teamA, teamB))
        );

        session.initializeSession();

        assertThrows(
                ValidationException.class,
                () -> matchFlowService.finishWithWinner(session, 3)
        );

        assertFalse(teamA.isPlayed());
        assertFalse(teamB.isPlayed());

        verify(teamCompletionService, never()) .complete(any(), any());
    }

    @Test
    @DisplayName("Should finish match with draw successfully")
    void shouldFinishMatchWithDrawSuccessfully() {
        Session session = new Session();

        Team teamA = createTeam(1, 4);
        Team teamB = createTeam(2, 4);
        Team waitingTeamA = createTeam(3, 4);
        Team waitingTeamB = createTeam(4, 4);

        session.setTeams(
                new ArrayList<>(List.of(teamA, teamB, waitingTeamA, waitingTeamB))
        );

        session.initializeSession();

        matchFlowService.finishWithDraw(session);

        Match currentMatch = session.getCurrentMatch();

        assertEquals(3, currentMatch.getTeamA().getNumber());
        assertEquals(4, currentMatch.getTeamB().getNumber());

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(2, waitingQueue.teams().size());

        assertEquals(1, waitingQueue.teams().get(0).getNumber());
        assertEquals(2, waitingQueue.teams().get(1).getNumber());

        assertTrue(teamA.isPlayed());
        assertTrue(teamB.isPlayed());

        verify(teamCompletionService) .complete(waitingTeamA, waitingQueue);
        verify(teamCompletionService) .complete(waitingTeamB, waitingQueue);
    }

    @Test
    @DisplayName("Should throw exception when queue has insufficient players for draw")
    void shouldThrowExceptionWhenQueueHasInsufficientPlayersForDraw() {
        Session session = new Session();

        Team teamA = createTeam(1, 4);
        Team teamB = createTeam(2, 4);
        Team waitingTeamA = createTeam(3, 4);
        Team waitingTeamB = createTeam(4, 2);

        session.setTeams(
                new ArrayList<>(List.of(teamA, teamB, waitingTeamA, waitingTeamB))
        );

        session.initializeSession();

        assertThrows(
                ConflictException.class,
                () -> matchFlowService.finishWithDraw(session)
        );
    }
}