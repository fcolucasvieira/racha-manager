package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.model.*;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemovePlayerFromSessionUseCaseTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private PriorityService priorityService;

    @InjectMocks
    private RemovePlayerFromSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    // helper
    private PlayerEntity createPlayer(String name) {
        return new PlayerEntity(UUID.randomUUID(), name);
    }

    // helper
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(createPlayer("P" + i));
        }

        return team;
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(sessionRepositoryPort, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPlayerIsNotInAnyTeam() {
        Session session = new Session();

        Team t1 = createTeam(1, 2);

        session.updateTeams(new ArrayList<>(List.of(t1)));

        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(sessionRepositoryPort, never()).save(any());
    }

    @Test
    void shouldRemovePlayerFromSession() {
        Session session = new Session();

        PlayerEntity removablePlayer =
                new PlayerEntity(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));

        session.updateTeams(new ArrayList<>(List.of(t1)));

        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        assertEquals(1, session.getActivePlayers().size());
        assertEquals(1, t1.getPlayers().size());

        verify(sessionRepositoryPort).save(session);
    }

    @Test
    void shouldDissolveEmptyTeam() {
        Session session = new Session();

        PlayerEntity removablePlayer = new PlayerEntity(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);

        session.updateTeams(new ArrayList<>(List.of(t1)));

        session.addPlayer(removablePlayer);

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        assertTrue(session.getTeams().isEmpty());

        verify(sessionRepositoryPort).save(session);
    }

    @Test
    void shouldApplyPriorityServiceWhenRemovingPlayerFromCurrentMatchTeam() {
        Session session = new Session();

        PlayerEntity removablePlayer = new PlayerEntity(playerId, "P1");

        Team t1 = new Team(1);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));
        t1.addPlayer(createPlayer("P3"));
        t1.addPlayer(createPlayer("P4"));

        session.updateTeams(new ArrayList<>(List.of(t1, t2, t3)));

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.startQueue();

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        verify(priorityService).apply(session);

        verify(sessionRepositoryPort).save(session);
    }

    @Test
    void shouldNotApplyPriorityServiceWhenRemovingPlayerOutsideCurrentMatch() {
        Session session = new Session();

        PlayerEntity removablePlayer = new PlayerEntity(playerId, "P1");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        Team t3 = new Team(3);

        t3.addPlayer(removablePlayer);
        t3.addPlayer(createPlayer("P2"));

        session.updateTeams(new ArrayList<>(List.of(t1, t2, t3)));

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.startQueue();

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        verify(priorityService, never()).apply(any());

        verify(sessionRepositoryPort).save(session);
    }

    @Test
    void shouldNotApplyPriorityServiceWhenSessionHasNotStarted() {
        Session session = new Session();

        PlayerEntity removablePlayer = new PlayerEntity(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));

        session.updateTeams(new ArrayList<>(List.of(t1)));

        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        verify(priorityService, never()).apply(any());
    }

    @Test
    void shouldThrowExceptionWhenRemovingLastPlayerFromCurrentMatchTeam() {
        Session session = new Session();

        PlayerEntity removablePlayer = new PlayerEntity(playerId, "P1");

        Team t1 = new Team(1);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        t1.addPlayer(removablePlayer);

        session.updateTeams(new ArrayList<>(List.of(t1, t2, t3)));

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.startQueue();

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(priorityService, never()).apply(any());

        verify(sessionRepositoryPort, never()).save(any());
    }
}