package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CurrentMatchRebalanceServiceTest {

    private CurrentMatchRebalanceService service;

    @BeforeEach
    void setup() {
        service = new CurrentMatchRebalanceService(new TeamFillService());
    }

    // helper (criação de times)
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        return team;
    }

    @Test
    @DisplayName("Success")
    void shouldFillIncompleteCurrentMatchTeams() {
        Session session = new Session();

        Team t1 = createTeam(1, 1);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams = new ArrayList<>(
                List.of(t1, t2, t3)
        );

        session.setTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(4)]
        session.initializeSession();

        service.apply(session);

        assertTrue(t1.isFull());

        assertEquals(4, t1.getPlayers().size());
        assertEquals(1, t3.getPlayers().size());

        assertEquals(4, t2.getPlayers().size());
    }

    @Test
    @DisplayName("Success (Fill with multiple teams)")
    void shouldPullPlayersFromMultipleTeams() {
        Session session = new Session();

        Team t1 = createTeam(1, 1);
        Team t2 = createTeam(2, 3);
        Team t3 = createTeam(3, 1);
        Team t4 = createTeam(4, 2);
        Team t5 = createTeam(5, 4);

        List<Team> teams = new ArrayList<>(
                List.of(t1, t2, t3, t4, t5)
        );

        session.setTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(1), t4(2), t5(4)]
        session.initializeSession();

        service.apply(session);

        assertTrue(t1.isFull());
        assertTrue(t2.isFull());

        assertEquals(1, session.getWaitingTeams().size());
        assertEquals(5, session.getWaitingTeams().get(0).getNumber());

        assertEquals(3, session.getWaitingTeams().get(0).getPlayers().size());

        assertEquals(3, session.getTeams().size());

        assertEquals(4, t1.getPlayers().size());
        assertEquals(4, t2.getPlayers().size());
    }

    @Test
    @DisplayName("Success (Dissolve empty teams)")
    void shouldDissolveEmptyTeams() {
        Session session = new Session();

        Team t1 = createTeam(1, 3);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);

        List<Team> teams = new ArrayList<>(
                List.of(t1, t2, t3)
        );

        session.setTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(1)]
        session.initializeSession();

        service.apply(session);

        assertTrue(t1.isFull());

        assertFalse(session.getTeams().contains(t3));
    }

    @Test
    @DisplayName("Success (No dissolve incomplete teams)")
    void shouldKeepIncompleteTeamsIfStillHasPlayers() {
        Session session = new Session();

        Team t1 = createTeam(1, 2);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 3);

        List<Team> teams = new ArrayList<>(
                List.of(t1, t2, t3)
        );

        session.setTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(3)]
        session.initializeSession();

        service.apply(session);

        assertTrue(t1.isFull());

        assertEquals(1, session.getWaitingTeams().size());

        assertEquals(1, t3.getPlayers().size());

        assertEquals(3, session.getTeams().size());
    }

    @Test
    @DisplayName("Return when session not started")
    void shouldDoNothingWhenSessionHasNotStarted() {
        Session session = new Session();

        assertDoesNotThrow(
                () -> service.apply(session)
        );

        assertNull(session.getCurrentMatch());
        assertTrue(session.getWaitingTeams().isEmpty());
        assertTrue(session.getTeams().isEmpty());
    }
}