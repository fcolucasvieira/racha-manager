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

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;
    @Mock
    private PlayerRepositoryPort playerRepositoryPort;
    @Mock
    private InitialTeamBalancerService initialTeamBalancerService;
    @Mock
    private PriorityService priorityService;

    @InjectMocks
    private AddPlayerToSessionUseCase useCase;

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

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));

        verify(playerRepositoryPort, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotFound() {
        Session session = new Session();

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));

        verify(sessionRepositoryPort, never()).save(any());
    }

    @Test
    void shouldCreateInitialTeamsWhenSessionReachEightPlayers() {
        Session session = new Session();

        for (int i = 1; i <= 7; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        PlayerEntity p8 = new PlayerEntity(playerId, "P8");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> generatedTeams = List.of(t1, t2);

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(p8));
        when(initialTeamBalancerService.createInitialTeams(session)).thenReturn(generatedTeams);

        List<Team> result = useCase.execute(sessionId, playerId);

        assertEquals(2, result.size());
        assertTrue(session.hasStarted());
        assertNotNull(session.getCurrentMatch());

        verify(initialTeamBalancerService).createInitialTeams(session);
        verify(sessionRepositoryPort).save(session);
    }

    @Test
    void shouldNotCreateInitialTeamsBeforeEightPlayers() {
        Session session = new Session();

        for (int i = 1; i <= 6; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        PlayerEntity p7 = new PlayerEntity(playerId, "P7");

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(p7));

        List<Team> result = useCase.execute(sessionId, playerId);

        assertTrue(result.isEmpty());

        verify(initialTeamBalancerService, never()).createInitialTeams(any());
        verify(sessionRepositoryPort).save(session);
    }

    @Test
    void shouldCreateNewTeamWhenLastTeamIsFull() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(new ArrayList<>(List.of(t1, t2)));

        session.startQueue();

        PlayerEntity newPlayer = new PlayerEntity(playerId, "P9");

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(newPlayer));

        useCase.execute(sessionId, playerId);

        Team createdTeam = session.getTeams().get(2);

        assertEquals(3, session.getTeams().size());
        assertEquals(3, createdTeam.getNumber());
        assertEquals(1, createdTeam.getPlayers().size());
        assertEquals("P9", createdTeam.getPlayers().get(0).getName());
    }

    @Test
    void shouldAddPlayerToIncompleteLastTeam() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 3);

        session.updateTeams(new ArrayList<>(List.of(t1, t2)));

        session.startQueue();

        PlayerEntity newPlayer = new PlayerEntity(playerId, "P8");

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(newPlayer));

        useCase.execute(sessionId, playerId);

        assertEquals(2, session.getTeams().size());
        assertEquals(4, session.getTeams().get(1).getPlayers().size());
    }

    @Test
    void shouldAddNewCreatedTeamToQueueWhenSessionAlreadyStarted() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(new ArrayList<>(List.of(t1, t2)));

        session.startQueue();

        PlayerEntity newPlayer = new PlayerEntity(playerId, "P9");

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(newPlayer));

        useCase.execute(sessionId, playerId);

        Team queuedTeam = session.getQueue().getFirst();

        assertEquals(1, session.getQueue().size());
        assertEquals(3, queuedTeam.getNumber());

        assertFalse(queuedTeam.isPlayed());
    }

    @Test
    void shouldAddNewCreatedRookieTeamBehindExistingRookieTeams() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.updateTeams(new ArrayList<>(List.of(t1, t2)));

        session.startQueue();

        PlayerEntity p9 = new PlayerEntity(UUID.randomUUID(), "P9");

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(p9));

        useCase.execute(sessionId, playerId);

        UUID secondPlayerId = UUID.randomUUID();

        PlayerEntity p10 = new PlayerEntity(secondPlayerId, "P10");

        Team incompleteTeam = session.getTeams().getLast();

        incompleteTeam.addPlayer(createPlayer("Extra1"));
        incompleteTeam.addPlayer(createPlayer("Extra2"));
        incompleteTeam.addPlayer(createPlayer("Extra3"));

        when(playerRepositoryPort.findById(secondPlayerId)).thenReturn(Optional.of(p10));

        useCase.execute(sessionId, secondPlayerId);

        assertEquals(2, session.getQueue().size());
        assertEquals(3, session.getQueue().get(0).getNumber());
        assertEquals(4, session.getQueue().get(1).getNumber());
    }

    @Test
    void shouldNotCreateInitialTeamsWhenSessionAlreadyShuffled() {
        Session session = new Session();

        for (int i = 1; i <= 7; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        session.markAsShuffled();

        PlayerEntity p8 = new PlayerEntity(playerId, "P8");

        when(sessionRepositoryPort.findById(sessionId)).thenReturn(Optional.of(session));

        when(playerRepositoryPort.findById(playerId)).thenReturn(Optional.of(p8));

        List<Team> result = useCase.execute(sessionId, playerId);

        assertTrue(result.isEmpty());
        assertFalse(session.hasStarted());

        verify(initialTeamBalancerService, never()).createInitialTeams(any());
    }
}