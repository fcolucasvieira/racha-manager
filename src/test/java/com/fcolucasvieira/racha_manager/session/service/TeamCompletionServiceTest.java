package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeamCompletionServiceTest {
    private TeamCompletionService teamCompletionService;

    @BeforeEach
    void setUp() {
        teamCompletionService = new TeamCompletionService();

    }

    // helper (criação de times)
    private Team createTeam(int number, int countPlayers) {

        Team team = new Team(number);

        for(int i = 1; i <= countPlayers; i++) {
            team.addPlayer(
                    new Player(
                            UUID.randomUUID(),
                            "P" + i)
            );
        }

        return team;
    }

    @Test
    @DisplayName("Team completed with single donor")
    void ShouldCompleteTeamWithSingleDonor() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 1);
        Team t3 = createTeam(3, 3);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.initializeSession();

        teamCompletionService.fill(t2, session.getWaitingTeams());

        assertTrue(t2.isFull());

        assertEquals(0, t3.getPlayers().size());
    }

    @Test
    @DisplayName("Team completed with multiple donors")
    void ShouldCompleteTeamWithMultiplesDonors() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 1);
        Team t3 = createTeam(3, 1);
        Team t4 = createTeam(4, 2);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3, t4))
        );

        session.initializeSession();

        teamCompletionService.fill(t2, session.getWaitingTeams());

        assertTrue(t2.isFull());

        assertEquals(0, t3.getPlayers().size());
        assertEquals(0, t4.getPlayers().size());
    }

    @Test
    @DisplayName("Team full not completed")
    void ShouldNotTransferPlayersWhenTargetIsFull() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.initializeSession();

        teamCompletionService.fill(t2, session.getWaitingTeams());

        assertTrue(t2.isFull());

        assertEquals(1, t3.getPlayers().size());
    }

    @Test
    @DisplayName("Team incomplete when donors have insufficient players")
    void shouldKeepTeamIncompleteWhenDonorsAreInsufficient() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 1);
        Team t3 = createTeam(3, 1);
        Team t4 = createTeam(4, 1);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3, t4))
        );

        session.initializeSession();

        teamCompletionService.fill(t2, session.getWaitingTeams());

        assertEquals(3, t2.getPlayers().size());

        assertEquals(0, t3.getPlayers().size());
        assertEquals(0, t4.getPlayers().size());
    }

    @Test
    @DisplayName("Team incomplete when queue is empty")
    void shouldKeepTargetUnchangedWhenQueueIsEmpty() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 2);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.initializeSession();

        teamCompletionService.fill(t2, session.getWaitingTeams());

        assertEquals(2, t2.getPlayers().size());
    }

    @Test
    @DisplayName("Team dissolved outside of current match")
    void shouldDissolveEmptyTeamOutsideCurrentMatch() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 0);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.initializeSession();

        teamCompletionService.dissolveEmptyTeams(session);

        assertFalse(session.getTeams().contains(t3));
    }

    @Test
    @DisplayName("Team not dissolved inside of current match")
    void shouldNotDissolveEmptyTeamInsideCurrentMatch() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 0);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.initializeSession();

        teamCompletionService.dissolveEmptyTeams(session);

        assertTrue(session.getTeams().contains(t2));
    }

    @Test
    @DisplayName("Team not dissolved when contains players")
    void shouldNotDissolveTeamWithPlayers() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.initializeSession();

        teamCompletionService.dissolveEmptyTeams(session);

        assertTrue(session.getTeams().contains(t3));
    }
}