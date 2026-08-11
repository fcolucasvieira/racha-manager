package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    private Session session;

    @BeforeEach
    void setup() {
        session = new Session();
    }

    private Player createPlayer() {
        return new Player(
                UUID.randomUUID(),
                "Player"
        );
    }

    private Team createTeam(int number) {
        Team team = new Team(number);

        for (int i = 1; i <= 4; i++) {
            team.addPlayer(createPlayer());
        }

        return team;
    }

    @Test
    void shouldNotAddDuplicatedPlayer() {
        Player player = createPlayer();

        session.addPlayer(player);

        assertThrows(
                ConflictException.class,
                () -> session.addPlayer(player)
        );
    }

    @Test
    void shouldNotAddPlayerNull() {
        Player player = null;

        assertThrows(
                ValidationException.class,
                () -> session.addPlayer(player)
        );
    }

    @Test
    void shouldNotRemovePlayerWhenPlayerNotExists() {
        UUID playerId = UUID.randomUUID();

        assertThrows(
                NotFoundException.class,
                () -> session.removePlayer(playerId)
        );
    }

    @Test
    void shouldNotRemovePlayerWithIdNull() {
        UUID playerId = null;

        assertThrows(
                ValidationException.class,
                () -> session.removePlayer(playerId)
        );
    }

    @Test
    void shouldNotSetTeamsWhenTeamsListBeNull() {
        List<Team> teams = null;

        assertThrows(
                ValidationException.class,
                () -> session.setTeams(teams)
        );
    }

    @Test
    void shouldNotRemoveTeamWhenTeamBeNull() {
        assertThrows(
                ValidationException.class,
                () -> session.removeTeam(null)
        );
    }

    @Test
    void shouldNotRemoveTeamWhenTeamIsInCurrentMatch() {
        Team t1 = new Team(1);
        Team t2 = new Team(2);
        Team t3 = new Team(3);

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.initializeSession();

        assertThrows(
                ConflictException.class,
                () -> session.removeTeam(t1)
        );
    }

    @Test
    void shouldNotSetCurrentMatchWhenMatchBeNull() {
        Match match = null;

        assertThrows(
                ValidationException.class,
                () -> session.setCurrentMatch(match)
        );
    }

    @Test
    void shouldInitializeSessionSuccessfully() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.initializeSession();

        assertNotNull(session.getCurrentMatch());

        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(2, session.getCurrentMatch().getTeamB().getNumber());

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(1, waitingQueue.getTeams().size());

        assertEquals(3, waitingQueue.getTeams().getFirst().getNumber());
    }

    @Test
    void shouldNotInitializeSessionWithLessThanTwoTeams() {
        Team t1 = createTeam(1);

        session.setTeams(
                new ArrayList<>(List.of(t1))
        );

        assertThrows(
                ConflictException.class,
                () -> session.initializeSession()
        );
    }

    @Test
    void shouldNotInitializeSessionAlreadyStarted() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.initializeSession();

        assertThrows(
                ConflictException.class,
                () -> session.initializeSession()
        );
    }

    @Test
    void shouldMarkSessionAsShuffled() {
        assertFalse(session.isInitialTeamsCreated());

        session.markInitialTeamsAsCreated();

        assertTrue(session.isInitialTeamsCreated());
    }
}