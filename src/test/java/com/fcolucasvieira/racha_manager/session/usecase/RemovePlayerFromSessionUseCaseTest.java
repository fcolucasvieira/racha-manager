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
    private RemovePlayerFromSessionUseCase removePlayerFromSessionUseCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    private Player createPlayer(String name) {
        return new Player(UUID.randomUUID(), name);
    }

    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(createPlayer("P" + i));
        }
        return team;
    }

    @Test
    @DisplayName("Should throw exception when session does not exist")
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> removePlayerFromSessionUseCase.execute(sessionId, playerId)
        );

        verify(sessionRepository, never()).save(any());
        verify(teamCompletionService, never()).complete(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when player is not in any team")
    void shouldThrowExceptionWhenPlayerIsNotInAnyTeam() {
        Session session = new Session();

        Team t1 = createTeam(1, 1);

        session.setTeams(List.of(t1));

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                NotFoundException.class,
                () -> removePlayerFromSessionUseCase.execute(sessionId, playerId)
        );

        verify(sessionRepository, never()).save(any());
        verify(teamCompletionService, never()).complete(any(), any());
    }

    @Test
    @DisplayName("Should remove player successfully")
    void shouldRemovePlayerSuccessfully() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));

        session.setTeams(List.of(t1));

        session.getTeams()
                .forEach(t -> t.getPlayers()
                        .forEach(session::addPlayer)
                );

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        removePlayerFromSessionUseCase.execute(sessionId, playerId);

        assertFalse(t1.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId))
        );

        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should complete affected when player belongs to current match")
    void shouldCompleteCurrentMatchTeamsWhenPlayerBelongsToCurrentMatch() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team teamA = new Team(1);
        Team teamB = createTeam(2, 4);
        Team waiting = createTeam(3, 4);

        teamA.addPlayer(removablePlayer);
        teamA.addPlayer(createPlayer("P2"));
        teamA.addPlayer(createPlayer("P3"));
        teamA.addPlayer(createPlayer("P4"));

        session.setTeams(List.of(teamA, teamB, waiting));

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        removePlayerFromSessionUseCase.execute(sessionId, playerId);

        verify(teamCompletionService).complete(eq(teamA), any());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should not complete affected team when removed player is outside current match")
    void shouldNotCompleteCurrentMatchTeamsWhenRemovePlayerIsOutsideCurrentMatch() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team teamA = createTeam(1, 4);
        Team teamB = createTeam(2, 4);

        Team waiting = new Team(3);
        waiting.addPlayer(removablePlayer);
        waiting.addPlayer(createPlayer("P2"));

        session.setTeams(List.of(teamA, teamB, waiting));

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        session.initializeSession();

        var previousMatch = session.getCurrentMatch();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        removePlayerFromSessionUseCase.execute(sessionId, playerId);

        verify(teamCompletionService, never()).complete(any(), any());

        assertSame(
                previousMatch,
                session.getCurrentMatch()
        );

        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should not complete teams when session has not started")
    void shouldNotCompleteTeamsWhenSessionHasNotStarted() {
        Session session = new Session();

        Player removablePlayer = new Player(playerId, "P1");

        Team t1 = new Team(1);

        t1.addPlayer(removablePlayer);
        t1.addPlayer(createPlayer("P2"));

        session.setTeams(List.of(t1));

        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        removePlayerFromSessionUseCase.execute(sessionId, playerId);

        verify(teamCompletionService, never()).complete(any(), any());
        verify(sessionRepository).save(session);
    }
}
