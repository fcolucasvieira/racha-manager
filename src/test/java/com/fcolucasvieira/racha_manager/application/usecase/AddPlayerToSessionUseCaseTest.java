package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.model.*;
import com.fcolucasvieira.racha_manager.domain.port.*;
import com.fcolucasvieira.racha_manager.domain.service.InitialTeamBalancerService;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPlayerToSessionUseCaseTest {

    @Mock private SessionRepositoryPort sessionRepositoryPort;
    @Mock private PlayerRepositoryPort playerRepositoryPort;
    @Mock private InitialTeamBalancerService initialTeamBalancerService;
    @Mock private PriorityService priorityService;

    @InjectMocks private AddPlayerToSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    // helper
    private PlayerEntity createPlayer(String name) {
        return new PlayerEntity(
                UUID.randomUUID(),
                name
        );
    }

    // helper
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(
                    createPlayer("P" + i)
            );
        }

        return team;
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // arrange
        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.empty());

        // act & assert
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(playerRepositoryPort, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotFound() {
        // arrange
        Session session = new Session();

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId))
                .thenReturn(Optional.empty());

        // act & assert
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(sessionRepositoryPort, never()).save(any());
    }

    @Test
    void shouldCreateInitialTeamsWhenSessionReachEightPlayers() {
        // arrange
        Session session = new Session();

        for (int i = 1; i <= 7; i++) {
            session.addPlayer(
                    createPlayer("P" + i)
            );
        }

        PlayerEntity p8 =
                new PlayerEntity(playerId, "P8");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> generatedTeams =
                List.of(t1, t2);

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId))
                .thenReturn(Optional.of(p8));

        when(initialTeamBalancerService.createInitialTeams(session))
                .thenReturn(generatedTeams);

        // act
        List<Team> result =
                useCase.execute(sessionId, playerId);

        // assert
        assertEquals(2, result.size());

        assertTrue(session.hasStarted());

        assertNotNull(session.getCurrentMatch());

        verify(initialTeamBalancerService)
                .createInitialTeams(session);

        verify(sessionRepositoryPort)
                .save(session);

        verify(priorityService, never())
                .apply(any());
    }

    @Test
    void shouldNotCreateInitialTeamsBeforeEightPlayers() {
        // arrange
        Session session = new Session();

        for (int i = 1; i <= 6; i++) {
            session.addPlayer(
                    createPlayer("P" + i)
            );
        }

        PlayerEntity p7 =
                new PlayerEntity(playerId, "P7");

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId))
                .thenReturn(Optional.of(p7));

        // act
        List<Team> result =
                useCase.execute(sessionId, playerId);

        // assert
        assertTrue(result.isEmpty());

        verify(initialTeamBalancerService, never())
                .createInitialTeams(any());

        verify(priorityService, never())
                .apply(any());

        verify(sessionRepositoryPort)
                .save(session);
    }

    @Test
    void shouldApplyPriorityServiceWhenSessionHasStarted() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.startQueue();

        PlayerEntity p9 =
                new PlayerEntity(playerId, "P9");

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId))
                .thenReturn(Optional.of(p9));

        // act
        useCase.execute(sessionId, playerId);

        // assert
        verify(priorityService)
                .apply(session);

        verify(sessionRepositoryPort)
                .save(session);
    }

    @Test
    void shouldCreateNewTeamWhenLastTeamIsFull() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.startQueue();

        PlayerEntity newPlayer =
                new PlayerEntity(playerId, "P9");

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId))
                .thenReturn(Optional.of(newPlayer));

        // act
        useCase.execute(sessionId, playerId);

        // assert
        assertEquals(3, session.getTeams().size());

        Team createdTeam =
                session.getTeams().get(2);

        assertEquals(3, createdTeam.getNumber());

        assertEquals(1, createdTeam.getPlayers().size());

        assertEquals(
                "P9",
                createdTeam.getPlayers().get(0).getName()
        );

        verify(priorityService).apply(session);
    }

    @Test
    void shouldAddPlayerToIncompleteLastTeam() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 3);

        session.updateTeams(
                new ArrayList<>(List.of(t1, t2))
        );

        session.startQueue();

        PlayerEntity newPlayer =
                new PlayerEntity(playerId, "P8");

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId))
                .thenReturn(Optional.of(newPlayer));

        // act
        useCase.execute(sessionId, playerId);

        // assert
        assertEquals(2, session.getTeams().size());

        assertEquals(
                4,
                session.getTeams().get(1).getPlayers().size()
        );

        verify(priorityService).apply(session);
    }
}