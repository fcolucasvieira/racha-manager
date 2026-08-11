package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.rules.RachaRules.*;
import static org.junit.jupiter.api.Assertions.*;

class InitialTeamsBalancerServiceTest {
    private InitialTeamsBalancerService initialTeamsBalancerService;

    @BeforeEach
    void setup() {
        initialTeamsBalancerService = new InitialTeamsBalancerService();
    }

    void addPlayers(Session session, int countPlayers) {
        for (int i = 1; i <= countPlayers; i++) {
            session.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }
    }

    @Test
    @DisplayName("Should create and balance the initial teams")
    void shouldBalanceInitialTeams() {
        Session session = new Session();

        addPlayers(session, INITIAL_PLAYERS);

        List<Team> teams = initialTeamsBalancerService.createInitialTeams(session);

        assertEquals(INITIAL_TEAMS, teams.size());

        assertEquals(1, teams.get(0).getNumber());
        assertEquals(2, teams.get(1).getNumber());

        assertTrue(teams.get(0).isFull());
        assertTrue(teams.get(1).isFull());

        assertEquals(TEAM_SIZE, teams.get(0).playersCount());
        assertEquals(TEAM_SIZE, teams.get(1).playersCount());

        int totalPlayers = teams.stream()
                .mapToInt(Team::playersCount)
                .sum();

        assertEquals(INITIAL_PLAYERS, totalPlayers);

        assertTrue(session.isInitialTeamsCreated());
    }

    @Test
    @DisplayName("Should throw exception when session is null")
    void shouldThrowExceptionWhenSessionIsNull() {
        assertThrows(
                ValidationException.class,
                () -> initialTeamsBalancerService.createInitialTeams(null)
        );
    }

    @Test
    @DisplayName("Should throw exception when session was already shuffled")
    void shouldThrowExceptionWhenSessionAlreadyShuffled(){
        Session session = new Session();

        session.markInitialTeamsAsCreated();

        assertThrows(
                ConflictException.class,
                () -> initialTeamsBalancerService.createInitialTeams(session)
        );
    }

    @Test
    @DisplayName("Should throw exception when session has fewer than initial players")
    void shouldThrowExceptionWhenSessionHasFewerThanInitialPlayers(){
        Session session = new Session();

        addPlayers(session, INITIAL_PLAYERS - 1);

        assertThrows(
                ConflictException.class,
                () -> initialTeamsBalancerService.createInitialTeams(session));
    }

    @Test
    @DisplayName("Should throw exception when session has more than initial players")
    void shouldThrowExceptionWhenSessionHasMoreThanInitialPlayers(){
        Session session = new Session();

        addPlayers(session, INITIAL_PLAYERS + 1);

        assertThrows(
                ConflictException.class,
                () -> initialTeamsBalancerService.createInitialTeams(session));
    }

    @Test
    @DisplayName("Should throw exception when session already contains teams")
    void shouldThrowExceptionWhenTeamsAlreadyExist() {
        Session session = new Session();

        addPlayers(session, INITIAL_PLAYERS);

        session.setTeams(
                List.of(new Team(1), new Team(2))
        );

        assertThrows(
                ConflictException.class,
                () -> initialTeamsBalancerService.createInitialTeams(session)
        );
    }
}
