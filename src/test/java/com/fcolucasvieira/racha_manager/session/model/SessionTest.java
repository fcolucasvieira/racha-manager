package com.fcolucasvieira.racha_manager.session.model;

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

    // helper (criar jogador)
    private Player createPlayer() {
        return new Player(
                UUID.randomUUID(),
                "Player"
        );
    }

    // helper (criar time)
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

    // Como esta validação é desnecessária, futuramente, teste será removido
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
    void shouldFindPlayerTeamSuccessfully() {
        UUID playerId = UUID.randomUUID();

        Player player = new Player(playerId, "Player");

        Team team = new Team(1);

        team.addPlayer(player);

        session.setTeams(
                new ArrayList<>(List.of(team))
        );

        Team result = session.findPlayerTeam(playerId);

        assertEquals(result, team);
        assertTrue(team.getPlayers().contains(player));
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

        session.startQueue();

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
    void shouldStartQueueSuccessfully() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.startQueue();

        assertNotNull(session.getCurrentMatch());

        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(2, session.getCurrentMatch().getTeamB().getNumber());

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(1, waitingQueue.getTeams().size());

        assertEquals(3, waitingQueue.getTeams().getFirst().getNumber());
    }

    @Test
    void shouldNotStartQueueWithLessThanTwoTeams() {
        Team t1 = createTeam(1);

        session.setTeams(
                new ArrayList<>(List.of(t1))
        );

        assertThrows(
                ConflictException.class,
                () -> session.startQueue()
        );
    }

    @Test
    void shouldNotStartQueueWhenQueueAlreadyStarted() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.startQueue();

        assertThrows(
                ConflictException.class,
                () -> session.startQueue()
        );
    }

    // Teste deve ser reajustado para classe de testes da WaitingQueue
    @Test
    void shouldPrioritizeRookieTeamBeforePlayedTeams() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        t3.markAsPlayed();

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.startQueue();

        Team t4 = createTeam(4);

        session.addTeamToQueue(t4);

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(4, waitingQueue.getTeams().get(0).getNumber());
        assertEquals(3, waitingQueue.getTeams().get(1).getNumber());
    }

    // Teste deve ser reajustado para classe de testes da WaitingQueue
    @Test
    void shouldAddPlayedTeamAtEndOfQueue() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.startQueue();

        Team t4 = createTeam(4);

        t4.markAsPlayed();

        session.addTeamToQueue(t4);

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(2, waitingQueue.getTeams().size());

        assertEquals(3, waitingQueue.getTeams().get(0).getNumber());
        assertEquals(4, waitingQueue.getTeams().get(1).getNumber());
    }

    // Teste deve ser reajustado para classe de testes da WaitingQueue
    @Test
    void shouldNotAddDuplicatedTeamToQueue() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.startQueue();

        assertThrows(
                ConflictException.class,
                () -> session.addTeamToQueue(t3)
        );
    }

    // Teste deve ser reajustado para classe de testes da WaitingQueue
    @Test
    void shouldNotAddTeamToQueueWhenTeamBeNull() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);

        session.setTeams(
                List.of(t1, t2)
        );

        session.startQueue();

        assertThrows(
                ValidationException.class,
                () -> session.addTeamToQueue(null)
        );
    }


    // Teste deve ser reajustado para classe de testes da WaitingQueue
    @Test
    void shouldNotAddTeamToQueueWhenQueueNotInitialized() {
        Team team = createTeam(1);

        assertThrows(
                ConflictException.class,
                () -> session.addTeamToQueue(team)
        );
    }

    // Teste deve ser reajustado para classe de testes da WaitingQueue
    @Test
    void shouldNotRemoveFirstTeamFromQueueWhenQueueIsEmpty() {
        assertThrows(
                ConflictException.class,
                () -> session.removeFirstTeamFromQueue()
        );
    }

    @Test
    void shouldRemoveFirstTeamFromQueue() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.startQueue();

        Team removed = session.removeFirstTeamFromQueue();

        assertEquals(3, removed.getNumber());

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertTrue(waitingQueue.getTeams().isEmpty());
    }

    @Test
    void shouldMarkSessionAsShuffled() {
        assertFalse(session.isShuffled());

        session.markAsShuffled();

        assertTrue(session.isShuffled());
    }
}