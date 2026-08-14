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

class WaitingQueueTest {
    private WaitingQueue waitingQueue;

    @BeforeEach
    void setup() {
        waitingQueue = new WaitingQueue(
                new ArrayList<>()
        );
    }

    private Team createTeam(int number) {
        Team team = new Team(number);

        for(int i = 1; i <= 4; i++){
            team.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        return team;
    }

    @Test
    void shouldNotAddTeamWhenItIsNull() {
        Team team = null;

        assertThrows(
                ValidationException.class,
                () -> waitingQueue.add(team)
        );
    }

    @Test
    void shouldNotAddADuplicateTeam() {
        Team team = createTeam(1);

        waitingQueue.add(team);

        assertThrows(
                ConflictException.class,
                () -> waitingQueue.add(team)
        );
    }

    @Test
    void shouldAddRookieTeamAtTheStartWhenThereAreNoOtherRookies() {
        Team t1 = createTeam(1);
        t1.markAsPlayed();

        waitingQueue.add(t1);

        Team t2 = createTeam(2);

        waitingQueue.add(t2);

        assertEquals(2, waitingQueue.getTeams().size());

        assertEquals(t2, waitingQueue.getTeams().get(0));
        assertEquals(t1, waitingQueue.getTeams().get(1));
    }

    @Test
    void shouldAddRookieTeamAfterLastRookieTeam() {
        Team t1 = createTeam(1);
        t1.markAsPlayed();

        Team t2 = createTeam(2);

        waitingQueue.add(t1);
        waitingQueue.add(t2);

        // Time novato a ser adicionado
        Team t3 = createTeam(3);

        waitingQueue.add(t3);

        assertEquals(3, waitingQueue.getTeams().size());

        assertEquals(t2, waitingQueue.getTeams().get(0));
        assertEquals(t3, waitingQueue.getTeams().get(1));
        assertEquals(t1, waitingQueue.getTeams().get(2));
    }

    @Test
    void shouldAddANonRookieTeamAfterLastRookieTeam() {
        Team t1 = createTeam(1);

        waitingQueue.add(t1);

        Team t2 = createTeam(2);
        t2.markAsPlayed();

        waitingQueue.add(t2);

        assertEquals(2, waitingQueue.getTeams().size());

        assertEquals(t1, waitingQueue.getTeams().get(0));
        assertEquals(t2, waitingQueue.getTeams().get(1));
    }

    @Test
    void shouldNotRemoveNullTeam() {
        assertThrows(
                ValidationException.class,
                () -> waitingQueue.remove(null)
        );
    }

    @Test
    void shouldNotRemoveTeamThatDoesNotExist() {
        Team team = createTeam(1);

        assertThrows(
                NotFoundException.class,
                () -> waitingQueue.remove(team)
        );
    }

    @Test
    void shouldRemoveExistingTeam() {
        Team team = createTeam(1);

        waitingQueue.add(team);

        waitingQueue.remove(team);

        assertTrue(waitingQueue.getTeams().isEmpty());
    }

    @Test
    void shouldNotPollWhenQueueIsEmpty() {
        assertThrows(
                ValidationException.class,
                () -> waitingQueue.poll()
        );
    }

    @Test
    void shouldPollFirstTeamAndRemoveIt() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);

        waitingQueue.add(t1);
        waitingQueue.add(t2);

        Team polled = waitingQueue.poll();

        assertEquals(t1, polled);

        assertEquals(1, waitingQueue.getTeams().size());

        assertEquals(t2, waitingQueue.getTeams().getFirst());
    }

    @Test
    void shouldCountPlayersCorrectly() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);

        waitingQueue.add(t1);
        waitingQueue.add(t2);

        assertEquals(8, waitingQueue.getPlayersCount());
    }

    @Test
    void shouldReturnTrueWhenHasEnoughPlayersForDraw() {
        Team t1 = createTeam(1);
        Team t2 = createTeam(2);
        Team t3 = createTeam(3);

        waitingQueue.add(t1);
        waitingQueue.add(t2);
        waitingQueue.add(t3);

        assertTrue(waitingQueue.hasEnoughForDraw());
    }

    @Test
    void shouldReturnFalseWhenNotEnoughPlayersForDraw() {
        Team t1 = createTeam(1);

        waitingQueue.add(t1);

        assertFalse(waitingQueue.hasEnoughForDraw());
    }

    @Test
    void shouldReturnImmutableListFromGetTeams() {
        Team t1 = createTeam(1);

        waitingQueue.add(t1);

        List<Team> list = waitingQueue.getTeams();

        assertEquals(1, list.size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> list.add(createTeam(2))
        );
    }
}