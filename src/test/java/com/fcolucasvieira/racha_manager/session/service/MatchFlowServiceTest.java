package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
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

    // helper (criação de times)
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
    void shouldFinishMatchWithWinnerSuccessfullyWithTeamsInQueue() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 2);

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.initializeSession();

        matchFlowService.finishWithWinner(session, 1);

        Match currentMatch = session.getCurrentMatch();

        assertEquals(1, currentMatch.getTeamA().getNumber());
        assertEquals(3, currentMatch.getTeamB().getNumber());

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(1, waitingQueue.getTeams().size());
        assertEquals(2, waitingQueue.getTeams().getFirst().getNumber());

        assertTrue(t1.isPlayed());
        assertTrue(t2.isPlayed());

        verify(teamCompletionService).complete(t3, waitingQueue);
    }

    @Test
    void shouldFinishMatchWithWinnerSuccessfullyWithoutTeamsInQueue() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(
                List.of(t1, t2)
        );

        session.initializeSession();

        matchFlowService.finishWithWinner(session, 1);

        Match currentMatch = session.getCurrentMatch();

        assertEquals(1, currentMatch.getTeamA().getNumber());
        assertEquals(2, currentMatch.getTeamB().getNumber());

        assertTrue(session.getWaitingTeams().isEmpty());

        assertTrue(t1.isPlayed());
        assertTrue(t2.isPlayed());

        verify(teamCompletionService, never()).complete(any(), any());

    }

    @Test
    void shouldThrowExceptionWhenNoMatchInProgress() {
        Session session = new Session();

        assertThrows(
                ConflictException.class,
                () -> matchFlowService.finishWithWinner(session, 1)
        );
    }

    @Test
    void shouldThrowExceptionWhenWinnerIsInvalid() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2));

        session.setTeams(teams);

        session.initializeSession();

        assertThrows(
                ValidationException.class,
                () -> matchFlowService.finishWithWinner(session, 3)
        );
    }

    @Test
    void shouldFinishMatchWithDrawSuccessfully() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 4);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3, t4))
        );

        // currentMatch -> t1 vs t2
        // queue -> [t3, t4]
        session.initializeSession();

        matchFlowService.finishWithDraw(session);

        assertEquals(3, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(4, session.getCurrentMatch().getTeamB().getNumber());

        assertEquals(2, session.getWaitingTeams().size());

        assertEquals(1, session.getWaitingTeams().get(0).getNumber());
        assertEquals(2, session.getWaitingTeams().get(1).getNumber());

        assertTrue(t1.isPlayed());
        assertTrue(t2.isPlayed());
    }

    @Test
    void shouldThrowExceptionWhenQueueHasInsufficientPlayersForDraw() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 2);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3, t4))
        );

        // currentMatch -> t1 vs t2
        // queue -> [t3, t4]
        session.initializeSession();

        assertThrows(
                ConflictException.class,
                () -> matchFlowService.finishWithDraw(session)
        );
    }
}