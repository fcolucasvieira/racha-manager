package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.service.TeamCompletionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private SessionRepository sessionRepository;
    @Mock
    private TeamCompletionService teamCompletionService;

    @InjectMocks
    private RemovePlayerFromSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    // helper (criação de jogador)
    private Player createPlayer(String name) {
        return new Player(UUID.randomUUID(), name);
    }

    // helper (criação de time)
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(createPlayer("P" + i));
        }
        return team;
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(sessionRepository, never()).save(any());
        verify(teamCompletionService, never()).complete(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenPlayerIsNotInAnyTeam() {
        Session session = new Session();

        Team t1 = createTeam(1, 1);

        session.setTeams(
                List.of(t1)
        );

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(sessionRepository, never()).save(any());
        verify(teamCompletionService, never()).complete(any(), any());
    }

    @Test
    void shouldRemovePlayerSuccessfully() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);

        t1.addPlayer(createPlayer("P2"));

        session.setTeams(
                List.of(t1)
        );

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        assertEquals(1, session.getActivePlayers().size());
        assertEquals(1, t1.getPlayers().size());

        assertFalse(t1.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId))
        );

        verify(sessionRepository).save(session);
    }

    @Test
    void shouldDissolveEmptyTeam() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);

        session.setTeams(
                new ArrayList<>(List.of(t1))
        );

        session.addPlayer(removablePlayer);

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        assertTrue(session.getTeams().isEmpty());

        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Apply priority service when player is removed from current match")
    void shouldApplyPriorityServiceWhenRemovingPlayerFromCurrentMatchTeam() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = new Team(1);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));
        t1.addPlayer(createPlayer("P3"));
        t1.addPlayer(createPlayer("P4"));

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        verify(currentMatchRebalanceService).apply(session);
        verify(sessionRepository).save(session);
    }

    @Test
    void shouldNotApplyPriorityServiceWhenRemovingPlayerOutsideCurrentMatch() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = new Team(3);

        t3.addPlayer(removablePlayer);
        t3.addPlayer(createPlayer("P2"));

        session.setTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        verify(currentMatchRebalanceService, never()).apply(any());

        verify(sessionRepository).save(session);
    }

    @Test
    void shouldNotApplyPriorityServiceWhenSessionHasNotStarted() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));

        session.setTeams(new ArrayList<>(List.of(t1)));

        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(sessionId, playerId);

        verify(currentMatchRebalanceService, never()).apply(any());
        verify(sessionRepository).save(session);
    }
}
