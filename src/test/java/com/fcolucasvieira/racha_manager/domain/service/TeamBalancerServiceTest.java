package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeamBalancerServiceTest {

    private TeamBalancerService service;

    @BeforeEach
    void setup() {
        service = new TeamBalancerService();
    }

    // Helper
    private PlayerEntity createPlayer() {
        UUID playerId = UUID.randomUUID();
        return new PlayerEntity(playerId, "Player " + playerId);
    }

    @Test
    void shouldCreateTeamsWithFourPlayersEachOnFirstBalance() {
        // Arrange
        Session session = new Session();

        for (int i = 0; i < 8; i++) {
            session.addPlayer(createPlayer());
        }

        // Act
        List<Team> teams = service.balance(session);

        // Assert
        assertEquals(2, teams.size());
        assertEquals(4, teams.get(0).getPlayers().size());
        assertEquals(4, teams.get(1).getPlayers().size());
    }

    @Test
    void shouldNotShufflePlayersAfterFirstBalance() {
        // Arrange
        Session session = new Session();

        for (int i = 0; i < 8; i++) {
            session.addPlayer(createPlayer());
        }

        List<Team> firstBalance = service.balance(session);
        session.updateTeams(firstBalance);

        List<PlayerEntity> firstOrder = firstBalance.stream()
                .flatMap(t -> t.getPlayers().stream())
                .toList();

        // Act
        List<Team> secondBalance = service.balance(session);

        // Assert
        List<PlayerEntity> secondOrder = secondBalance.stream()
                .flatMap(t -> t.getPlayers().stream())
                .toList();

        assertEquals(firstOrder, secondOrder);
    }

    @Test
    void shouldPlaceNewPlayerAtEndOfQueue() {
        // Arrange
        Session session = new Session();

        for (int i = 0; i < 8; i++) {
            session.addPlayer(createPlayer());
        }

        session.updateTeams(service.balance(session));

        PlayerEntity newPlayer = createPlayer();

        // Act
        session.addPlayer(newPlayer);
        List<Team> teams = service.balance(session);

        // Assert
        List<PlayerEntity> allPlayers = teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .toList();

        assertEquals(newPlayer, allPlayers.get(allPlayers.size() - 1));
    }

    @Test
    void shouldRebalanceTeamsAfterPlayerRemoval() {
        // Arrange
        Session session = new Session();

        List<PlayerEntity> players = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            PlayerEntity player = createPlayer();
            players.add(player);
            session.addPlayer(player);
        }

        session.updateTeams(service.balance(session));

        UUID removedId = players.get(8).getId();

        // Act
        session.removePlayer(removedId);
        List<Team> teams = service.balance(session);

        // Assert
        assertEquals(2, teams.size());
        assertEquals(4, teams.get(0).getPlayers().size());
        assertEquals(4, teams.get(1).getPlayers().size());

        boolean stillExists = teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .anyMatch(p -> p.getId().equals(removedId));

        assertFalse(stillExists);
    }

    @Test
    void shouldMaintainQueueOrderWhenPlayersAreAdded() {
        // Arrange
        Session session = new Session();

        PlayerEntity p1 = createPlayer();
        PlayerEntity p2 = createPlayer();
        PlayerEntity p3 = createPlayer();
        PlayerEntity p4 = createPlayer();

        session.addPlayer(p1);
        session.addPlayer(p2);
        session.addPlayer(p3);
        session.addPlayer(p4);

        session.updateTeams(service.balance(session));

        PlayerEntity p5 = createPlayer();

        // Act
        session.addPlayer(p5);
        List<Team> teams = service.balance(session);

        List<PlayerEntity> result = teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .toList();

        // Assert
        assertEquals(List.of(p1, p2, p3, p4, p5), result);
    }
}
