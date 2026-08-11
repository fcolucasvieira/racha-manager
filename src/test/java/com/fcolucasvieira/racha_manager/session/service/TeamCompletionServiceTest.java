package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeamCompletionServiceTest {
    private TeamCompletionService teamCompletionService;

    @BeforeEach
    void setup() {
        teamCompletionService = new TeamCompletionService();
    }

    private Team createTeam(int number, int playerCount) {
        Team team = new Team(number);

        for (int i = 0; i < playerCount; i++) {
            team.addPlayer(new Player(UUID.randomUUID(), "P" + i));
        }
        return team;
    }

    @Test
    @DisplayName("Should transfer players until target team is full")
    void shouldTransferPlayersUntilTargetIsFull() {
        Team target = createTeam(1, 1);
        Team donor = createTeam(2, 4);

        WaitingQueue queue = new WaitingQueue(
                List.of(donor)
        );

        teamCompletionService.complete(target, queue);

        assertTrue(target.isFull());
        assertEquals(1, donor.playersCount());
    }

    @Test
    @DisplayName("Should stop transferring when target team becomes full")
    void shouldStopTransferringWhenTargetIsFullEvenIfDonorHasPlayers() {
        Team target = createTeam(1, 3);
        Team donor = createTeam(2, 4);

        WaitingQueue queue = new WaitingQueue(
                List.of(donor)
        );

        teamCompletionService.complete(target, queue);

        assertTrue(target.isFull());
        assertEquals(3, donor.playersCount());
    }

    @Test
    @DisplayName("Should transfer players from multiple donos teams when necessary")
    void shouldHandleMultipleDonors() {
        Team target = createTeam(1, 1);
        Team donor1 = createTeam(2, 2);
        Team donor2 = createTeam(3, 3);

        WaitingQueue queue = new WaitingQueue(
                List.of(donor1, donor2)
        );

        teamCompletionService.complete(target, queue);

        assertTrue(target.isFull());

        assertFalse(donor1.hasPlayers());

        assertEquals(2, donor2.playersCount());
    }

    @Test
    @DisplayName("Should keep target team incomplete when donors have no players")
    void shouldNotBreakWhenDonorHasNoPlayers() {
        Team target = createTeam(1, 2);
        Team donor = createTeam(2, 0);

        WaitingQueue queue = new WaitingQueue(List.of(donor));

        teamCompletionService.complete(target, queue);

        assertTrue(target.isIncomplete());
        assertFalse(donor.hasPlayers());
    }
}
