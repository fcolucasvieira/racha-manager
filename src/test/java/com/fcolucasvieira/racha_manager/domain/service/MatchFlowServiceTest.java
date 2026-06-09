package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.session.service.MatchFlowService;
import com.fcolucasvieira.racha_manager.player.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.service.TeamFillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.constant.SessionRules.TEAM_SIZE;
import static org.junit.jupiter.api.Assertions.*;

class MatchFlowServiceTest {

    private MatchFlowService service;

    @BeforeEach
    void setup() {
        service = new MatchFlowService(new TeamFillService());
    }

    // helper (criação de times)
    private Team createTeam(int number, int countPlayers) {
        Team team = new Team(number);

        for (int i = 1; i <= countPlayers; i++) {
            team.addPlayer(
                    new PlayerEntity(
                            UUID.randomUUID(),
                            "P" + i
                    )
            );
        }

        return team;
    }

    @Test
    @DisplayName("Success with teams in queue (Winner)")
    void shouldFinishMatchWithWinnerSuccessfullyWithTeamsInQueue() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2, t3));

        session.updateTeams(teams);

        session.startQueue();

        service.finishWithWinner(session, 1);

        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(3, session.getCurrentMatch().getTeamB().getNumber());

        assertEquals(1, session.getQueue().size());
        assertEquals(2, session.getQueue().get(0).getNumber());

        assertTrue(t1.isPlayed());
        assertTrue(t2.isPlayed());
    }

    @Test
    @DisplayName("Success without teams in queue (Winner)")
    void shouldFinishMatchWithWinnerSuccessfullyWithoutTeamsInQueue() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2));

        session.updateTeams(teams);

        session.startQueue();

        service.finishWithWinner(session, 1);

        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(2, session.getCurrentMatch().getTeamB().getNumber());

        assertTrue(session.getQueue().isEmpty());

        assertTrue(t1.isPlayed());
        assertTrue(t2.isPlayed());
    }

    @Test
    @DisplayName("Session not started (Winner & Draw)")
    void shouldThrowExceptionWhenNoMatchInProgress() {
        Session session = new Session();

        assertThrows(
                ConflictException.class,
                () -> service.finishWithWinner(session, 1)
        );
    }

    @Test
    @DisplayName("Session not initialized queue (Winner & Draw)")
    void shouldThrowExceptionWhenQueueNotInitialized() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(List.of(t1, t2));

        session.updateCurrentMatch(
                new Match(t1, t2)
        );

        assertThrows(
                ConflictException.class,
                () -> service.finishWithWinner(session, 1)
        );
    }

    @Test
    @DisplayName("Invalid winner to finish match (Winner)")
    void shouldThrowExceptionWhenWinnerIsInvalid() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2));

        session.updateTeams(teams);

        session.startQueue();

        assertThrows(
                ValidationException.class,
                () -> service.finishWithWinner(session, 3)
        );
    }

    @Test
    @DisplayName("Success (Draw)")
    void shouldFinishMatchWithDrawSuccessfully() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3, t4))
        );

        // currentMatch -> t1 vs t2
        // queue -> [t3, t4]
        session.startQueue();

        service.finishWithDraw(session);

        assertEquals(3, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(4, session.getCurrentMatch().getTeamB().getNumber());

        assertEquals(2, session.getQueue().size());

        assertEquals(1, session.getQueue().get(0).getNumber());
        assertEquals(2, session.getQueue().get(1).getNumber());

        assertTrue(t1.isPlayed());
        assertTrue(t2.isPlayed());
    }

    @Test
    @DisplayName("Queue has less than " + (TEAM_SIZE * 2) + " players available (Draw)")
    void shouldThrowExceptionWhenQueueHasInsufficientPlayersForDraw() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 2);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3, t4))
        );

        // currentMatch -> t1 vs t2
        // queue -> [t3, t4]
        session.startQueue();

        assertThrows(
                ConflictException.class,
                () -> service.finishWithDraw(session)
        );
    }
}