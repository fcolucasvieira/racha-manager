package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchFlowServiceTest {

    private MatchFlowService service;

    @BeforeEach
    void setup() {
        service = new MatchFlowService();
    }

    // helper para criação de times
    Team createTeam(int number, int countPlayers) {
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
    void shouldFinishMatchWithWinnerSuccessfully() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2, t3));

        session.updateTeams(teams);

        session.startQueue();

        // act
        service.finishWithWinner(session, 1);

        // assert
        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(3, session.getCurrentMatch().getTeamB().getNumber());

        assertEquals(1, session.getQueue().size());
        assertEquals(2, session.getQueue().get(0).getNumber());
    }

    @Test
    void shouldKeepSameMatchWhenQueueIsEmpty() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2));

        session.updateTeams(teams);

        session.startQueue();

        // act
        service.finishWithWinner(session, 1);

        // assert
        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(2, session.getCurrentMatch().getTeamB().getNumber());

        assertTrue(session.getQueue().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenNoMatchInProgress() {
        // arrange
        Session session = new Session();

        // act & assert
        assertThrows(IllegalStateException.class, () -> service.finishWithWinner(session, 1));
    }

    @Test
    void shouldThrowExceptionWhenQueueNotInitialized() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(List.of(t1, t2));

        session.updateCurrentMatch(
                new Match(t1, t2)
        );

        // act & assert
        assertThrows(IllegalStateException.class, () -> service.finishWithWinner(session, 1));
    }

    @Test
    void shouldThrowExceptionWhenWinnerIsInvalid() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams =
                new ArrayList<>(List.of(t1, t2));

        session.updateTeams(teams);

        session.startQueue();

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> service.finishWithWinner(session, 99));
    }
}