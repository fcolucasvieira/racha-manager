package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.service.InitialTeamsBalancerService;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InitialTeamsBalancerServiceTest {
    private InitialTeamsBalancerService service;

    @BeforeEach
    void setup() {
        service = new InitialTeamsBalancerService();
    }

    @Test
    @DisplayName("Success")
    void shouldCreateTwoTeamsFull() {
        Session session = new Session();

        // players suficientes adicionados a sessão
        for(int i = 1; i <= INITIAL_PLAYERS; i++){
            session.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i));
        }

        List<Team> teams = service.createInitialTeams(session);

        assertEquals(INITIAL_TEAMS, teams.size());

        assertEquals(1, teams.get(0).getNumber());
        assertEquals(2, teams.get(1).getNumber());

        assertEquals(TEAM_SIZE, teams.get(0).getPlayers().size());
        assertEquals(TEAM_SIZE, teams.get(1).getPlayers().size());

        int totalPlayers = teams.stream()
                .mapToInt(team -> team.getPlayers().size())
                .sum();

        assertEquals(INITIAL_PLAYERS, totalPlayers);

        assertTrue(session.isShuffled());
    }

    @Test
    @DisplayName("Session null")
    void shouldThrowExceptionWhenSessionIsNull() {
        assertThrows(
                ValidationException.class,
                () -> service.createInitialTeams(null)
        );
    }

    @Test
    @DisplayName("Session already is shuffled")
    void shouldThrowExceptionWhenSessionAlreadyShuffled(){
        Session session = new Session();

        session.markAsShuffled();

        assertThrows(ConflictException.class,
                () -> service.createInitialTeams(session));
    }

    @Test
    @DisplayName("Session less than quantity initial players")
    void shouldThrowExceptionWhenSessionHasLessThanInitialPlayers(){
        Session session = new Session();

        for(int i = 1; i <= INITIAL_PLAYERS - 1; i++) {
            session.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        assertThrows(ConflictException.class,
                () -> service.createInitialTeams(session));
    }

    @Test
    @DisplayName("Session more than quantity initial players")
    void shouldThrowExceptionWhenSessionHasMoreThanInitialPlayers(){
        Session session = new Session();

        for(int i = 1; i <= INITIAL_PLAYERS + 1; i++) {
            session.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        assertThrows(ConflictException.class,
                () -> service.createInitialTeams(session));
    }

    @Test
    @DisplayName("Session already contains teams")
    void shouldThrowExceptionWhenTeamsAlreadyExist() {
        Session session = new Session();

        for (int i = 1; i <= INITIAL_PLAYERS; i++) {
            session.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        session.updateTeams(List.of(
                new Team(1),
                new Team(2)
        ));

        assertThrows(
                ConflictException.class,
                () -> service.createInitialTeams(session)
        );
    }
}
