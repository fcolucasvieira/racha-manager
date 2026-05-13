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

class PriorityServiceTest {

    private PriorityService service;

    @BeforeEach
    void setup() {
        service = new PriorityService();
    }

    // helper
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i)
            );
        }

        return team;
    }

    @Test
    void shouldFillIncompleteCurrentMatchTeams() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 2);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        // adiciona times a sessão
        session.updateTeams(teams);

        // currentMatch -> t1 vs t2
        // inicia a queue -> [t3(4)]
        session.startQueue();

        // act
        service.apply(session);

        // assert
        assertTrue(t1.isFull());

        assertEquals(2, t3.getPlayers().size());
    }

    @Test
    void shouldPullPlayersFromMultipleTeams() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 1);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);
        Team t4 = createTeam(4, 2);
        Team t5 = createTeam(5, 4);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3, t4, t5));

        // adiciona times a sessão
        session.updateTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(1), t4(2), t5(4)]
        session.startQueue();

        // act
        service.apply(session);

        // assert
        assertTrue(t1.isFull());
        assertTrue(t5.isFull());

        assertEquals(1, session.getQueue().size());
        assertEquals(5, session.getQueue().get(0).getNumber());

        assertEquals(3, session.getTeams().size());

        assertTrue(
                session.getTeams()
                        .stream()
                        .anyMatch(team -> team.getNumber() == 5)
        );
    }

    @Test
    void shouldDissolveEmptyTeams() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 3);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        // adiciona times a sessão
        session.updateTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(1)]
        session.startQueue();

        // act
        service.apply(session);

        // assert
        assertTrue(t1.isFull());
        assertTrue(session.getQueue().isEmpty());

        assertEquals(2, session.getTeams().size());
    }

    @Test
    void shouldKeepIncompleteTeamsIfStillHasPlayers() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 2);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 3);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        // adiciona times a sessão
        session.updateTeams(teams);

        // currentMatch -> t1 vs t2
        // queue -> [t3(3)]
        session.startQueue();

        // act
        service.apply(session);

        // assert
        assertTrue(t1.isFull());

        assertEquals(1, session.getQueue().size());
        assertEquals(1, t3.getPlayers().size());
    }

    @Test
    void shouldDoNothingWhenCurrentMatchIsNull() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 2);
        Team t3 = createTeam(3, 4);


        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        // adiciona times a sessão
        session.updateTeams(teams);

        // act
        service.apply(session);

        // assert
        assertEquals(3, session.getTeams().size());
        assertEquals(2, t2.getPlayers().size());
    }
}