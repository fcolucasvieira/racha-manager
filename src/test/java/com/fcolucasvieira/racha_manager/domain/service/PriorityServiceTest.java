package com.fcolucasvieira.racha_manager.domain.service;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class PriorityServiceTest {
    private final PriorityService service = new PriorityService();

    // helpers
    private PlayerEntity player() {
        return new PlayerEntity(UUID.randomUUID(), "Lucas");
    }

    private Team createTeam(int number, int playersCount) {
        Team team = new Team(1);

        for(int i = 0; i < playersCount; i++){
            team.addPlayer(player());
        }

        return team;
    }

    @Test
    void shouldFillIncompleteTeamFromNextTeam() {
        // arrange
        Team t1 = createTeam(1, 2);
        Team t2 = createTeam(2, 4);

        List<Team> teams = List.of(t1, t2);

        // act
        service.apply(teams);

        // assert
        assertTrue(t1.isFull());
        assertTrue(t2.isIncomplete());
        assertEquals(4, t1.getPlayers().size());
        assertEquals(2, t2.getPlayers().size());
    }

    @Test
    void shouldFillIncompleteTeamsInCascade() {
        // arrange
        Team t1 = createTeam(1, 2);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams = List.of(t1, t2, t3);

        // act
        service.apply(teams);

        // assert
        assertTrue(t1.isFull());
        assertTrue(t2.isFull());
        assertTrue(t3.isIncomplete());
        assertEquals(2, t3.getPlayers().size());
    }

    @Test
    void shouldNotFillLastTeamIfIncomplete() {
        // arrange
        Team t1 = createTeam(1, 2);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 2);

        List<Team> teams = List.of(t1, t2, t3);

        // act
        service.apply(teams);

        // assert
        assertTrue(t1.isFull());
        assertTrue(t2.isFull());
        assertTrue(t3.isIncomplete());
        assertEquals(0, t3.getPlayers().size());
    }

    @Test
    void shouldHandleMultipleIncompleteTeams() {
        // arrange
        Team t1 = createTeam(1, 1);
        Team t2 = createTeam(2, 2);
        Team t3 = createTeam(3, 4);

        List<Team> teams = List.of(t1, t2, t3);

        // act
        service.apply(teams);

        // assert
        assertTrue(t1.isFull());
        assertTrue(t2.isIncomplete());
        assertEquals(3, t2.getPlayers().size());
        assertTrue(t3.isIncomplete());
        assertEquals(0, t3.getPlayers().size());
    }

    @Test
    void shouldDoNothingWhenAlreadyTeamsAreFull() {
        // arrange
        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams = List.of(t1, t2);

        // act
        service.apply(teams);

        // assert
        assertTrue(t1.isFull());
        assertTrue(t2.isFull());
    }
}