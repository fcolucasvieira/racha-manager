package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddPlayerToActiveSessionServiceTest {
    private AddPlayerToActiveSessionService addPlayerToActiveSessionService;

    @BeforeEach
    void setUp() {
        addPlayerToActiveSessionService = new AddPlayerToActiveSessionService();
    }

    UUID playerId = UUID.randomUUID();

    public Team createTeam(int number, int countPlayers) {
        Team team = new Team(number);

        for(int i = 1; i <= countPlayers; i++) {
            team.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        return team;
    }

    @Test
    @DisplayName("Should add player in last team incomplete")
    void shouldAddPlayerWhenLastTeamIsIncomplete() {
        Session session = new Session();

        Player aditionalPlayer = new Player(playerId, "Lucas");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team lastTeam = createTeam(3, 3);

        session.setTeams(new ArrayList<>(List.of(t1, t2, lastTeam)));

        session.initializeSession();

        addPlayerToActiveSessionService.addPlayer(session, aditionalPlayer);

        assertTrue(lastTeam.isFull());

        assertEquals(aditionalPlayer, lastTeam.getPlayers().getLast());
    }

    @Test
    @DisplayName("Should add player in waiting queue is empty")
    void shouldAddPlayerWhenLastTeamIsNull() {
        Session session = new Session();

        Player aditionalPlayer = new Player(playerId, "Lucas");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(new ArrayList<>(List.of(t1, t2)));

        session.initializeSession();

        addPlayerToActiveSessionService.addPlayer(session, aditionalPlayer);

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertFalse(waitingQueue.isEmpty());

        assertEquals(1, waitingQueue.teams().size());

        Team nextTeam = waitingQueue.teams().getFirst();

        assertEquals(1, nextTeam.playersCount());

        assertEquals(aditionalPlayer, nextTeam.getPlayers().getFirst());
    }

    @Test
    @DisplayName("Should add player when last team is full")
    void shouldCreateNextTeamAndAddPlayerWhenLastTeamIsFull() {
        Session session = new Session();

        Player aditionalPlayer = new Player(playerId, "Lucas");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team lastTeam = createTeam(3, 4);

        session.setTeams(new ArrayList<>(List.of(t1, t2, lastTeam)));

        session.initializeSession();

        addPlayerToActiveSessionService.addPlayer(session, aditionalPlayer);

        WaitingQueue waitingQueue = session.getWaitingQueue();

        assertEquals(2, waitingQueue.teams().size());

        Team nextTeam = waitingQueue.teams().getLast();

        assertEquals(4, nextTeam.getNumber());

        assertEquals(1, nextTeam.playersCount());

        assertEquals(aditionalPlayer, nextTeam.getPlayers().getFirst());
    }
}