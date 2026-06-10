package com.fcolucasvieira.racha_manager.domain.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
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

    // helper - criar jogador
    private Player createPlayer() {
        return new Player(
                UUID.randomUUID(),
                "Player"
        );
    }

    // helper - criar time
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(createPlayer());
        }

        return team;
    }

    @Test
    void shouldNotAddDuplicatedPlayer() {
        // arrange
        Player player = createPlayer();

        session.addPlayer(player);

        // act & assert
        assertThrows(
                ConflictException.class,
                () -> session.addPlayer(player)
        );
    }

    @Test
    void shouldNotAddPlayerNull() {
        // arrange
        Player player = null;

        assertThrows(
                ValidationException.class,
                () -> session.addPlayer(player)
        );
    }

    @Test
    void shouldNotAddPlayerWithIdNull() {
        Player player = new Player(null, "Player");

        assertThrows(
                ValidationException.class,
                () -> session.addPlayer(player)
        );
    }

    @Test
    void shouldNotRemovePlayerWhenPlayerNotExists() {
        // arrange
        UUID playerId = UUID.randomUUID();

        // act & assert
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
    void shouldfindPlayerTeamSuccessfully() {
        UUID playerId = UUID.randomUUID();

        Player player = new Player(playerId, "Player");

        Team team = new Team(1);

        team.addPlayer(player);

        session.updateTeams(
                new ArrayList<>(List.of(team))
        );

        Team result = session.findPlayerTeam(playerId);

        assertEquals(result, team);
        assertTrue(team.getPlayers().contains(player));
    }

    @Test
    void shouldNotUpdateTeamsWhenTeamsListBeNull() {
        List<Team> teams = null;

        assertThrows(
                ValidationException.class,
                () -> session.updateTeams(teams)
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
    void shouldNotUpdateCurrentMatchWhenMatchBeNull() {
        Match match = null;

        assertThrows(
                ValidationException.class,
                () -> session.updateCurrentMatch(match)
        );
    }

    @Test
    void shouldStartQueueSuccessfully() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        // act
        session.startQueue();

        // assert
        assertNotNull(session.getCurrentMatch());

        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(2, session.getCurrentMatch().getTeamB().getNumber());

        assertEquals(1, session.getQueue().size());

        assertEquals(3, session.getQueue().getFirst().getNumber());
    }

    @Test
    void shouldNotStartQueueWithLessThanTwoTeams() {
        // arrange
        Team t1 = createTeam(1, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1))
        );

        // act & assert
        assertThrows(ConflictException.class, () -> session.startQueue());
    }

    @Test
    void shouldNotStartQueueWhenQueueAlreadyStarted() {
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.startQueue();

        assertThrows(
                ConflictException.class,
                () -> session.startQueue()
        );
    }

    @Test
    void shouldPrioritizeRookieTeamBeforePlayedTeams() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        // t3 não jogou ainda
        Team t3 = createTeam(3, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.startQueue();

        Team t4 = createTeam(4, 4);

        // act
        session.addTeamToQueue(t4);

        // assert
        assertEquals(3, session.getQueue().get(0).getNumber());
        assertEquals(4, session.getQueue().get(1).getNumber());
    }

    @Test
    void shouldAddPlayedTeamAtEndOfQueue() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.startQueue();

        Team t4 = createTeam(4, 4);

        // t4 já participou de partidas
        t4.markAsPlayed();

        // act
        session.addTeamToQueue(t4);

        // assert
        assertEquals(2, session.getQueue().size());

        assertEquals(3, session.getQueue().get(0).getNumber());
        assertEquals(4, session.getQueue().get(1).getNumber());
    }

    @Test
    void shouldNotAddDuplicatedTeamToQueue() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.startQueue();

        // act & assert
        assertThrows(ConflictException.class, () -> session.addTeamToQueue(t3));
    }

    @Test
    void shouldNotAddTeamToQueueWhenTeamBeNull() {
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.startQueue();

        assertThrows(
                ValidationException.class,
                () -> session.addTeamToQueue(null)
        );
    }

    @Test
    void shouldNotAddTeamToQueueWhenQueueNotInitialized() {
        Team team = createTeam(1, 4);

        assertThrows(
                ConflictException.class,
                () -> session.addTeamToQueue(team)
        );
    }

    @Test
    void shouldNotRemoveFirstTeamFromQueueWhenQueueIsEmpty() {
        assertThrows(
                ConflictException.class,
                () -> session.removeFirstTeamFromQueue()
        );
    }

    @Test
    void shouldRemoveFirstTeamFromQueue() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.startQueue();

        // act
        Team removed = session.removeFirstTeamFromQueue();

        // assert
        assertEquals(3, removed.getNumber());

        assertTrue(session.getQueue().isEmpty());
    }

    @Test
    void shouldClearQueueSuccessfully() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.startQueue();

        // act
        session.clearQueue();

        // assert
        assertNull(session.getCurrentMatch());

        assertTrue(session.getQueue().isEmpty());
    }

    @Test
    void shouldMarkSessionAsShuffled() {
        assertFalse(session.isShuffled());

        session.markAsShuffled();

        assertTrue(session.isShuffled());
    }
}