package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InitialTeamBalancerServiceTest {
    private InitialTeamBalancerService service;

    @BeforeEach
    void setup() {
        service = new InitialTeamBalancerService();
    }

    @Test
    void shouldCreateTwoTeamsWithFourPlayersEach() {
        // ARRANGE

        // Sessão existente e ainda não balanceada
        Session session = new Session();

        // 8 players adicionados a sessão
        for(int i = 1; i <= 8; i++){
            session.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i));
        }

        // ACT
        List<Team> teams = service.createInitialTeams(session);

        // ASSERT
        assertEquals(2, teams.size());
        assertEquals(4, teams.get(0).getPlayers().size());
        assertEquals(4, teams.get(1).getPlayers().size());

        int totalPlayers = teams.stream()
                .mapToInt(team -> team.getPlayers().size())
                .sum();

        assertEquals(8, totalPlayers);
    }

    @Test
    void shouldMarkSessionAsShuffled() {
        Session session = new Session();

        for (int i = 1; i <= 8; i++) {
            session.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i)
            );
        }

        service.createInitialTeams(session);

        assertTrue(session.isShuffled());
    }

    @Test
    void shouldThrowExceptionWhenLessThanEightPlayers(){
        Session session = new Session();

        for(int i = 0; i < 7; i++) {
            session.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i)
            );
        }

        assertThrows(IllegalStateException.class,
                () -> service.createInitialTeams(session));
    }

    @Test
    void shouldThrowExceptionWhenSessionAlreadyShuffled(){
        Session session = new Session();

        session.markAsShuffled();

        assertThrows(IllegalStateException.class,
                    () -> service.createInitialTeams(session));
    }

    @Test
    void shouldNotAllowBalanceWhenSessionAlreadyHasTeams() {
        Session session = new Session();

        for (int i = 1; i <= 8; i++) {
            session.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i)
            );
        }

        session.updateTeams(List.of(
                new Team(1),
                new Team(2)
        ));

        assertThrows(
                IllegalStateException.class,
                () -> service.createInitialTeams(session)
        );
    }
}
