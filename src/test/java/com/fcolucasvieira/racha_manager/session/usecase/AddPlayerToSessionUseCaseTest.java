package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.session.service.InitialTeamsBalancerService;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.model.Team;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPlayerToSessionUseCaseTest {
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private InitialTeamsBalancerService initialTeamsBalancerService;

    @InjectMocks
    private AddPlayerToSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    // helper (criação de jogador)
    private Player createPlayer(String name) {
        return new Player(
                UUID.randomUUID(),
                name
        );
    }

    // helper (criação de times)
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for (int i = 1; i <= playersCount; i++) {
            team.addPlayer(createPlayer("P" + i));
        }

        return team;
    }

    @Test
    @DisplayName("Session not found")
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(playerRepository, never()).findById(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Player not found")
    void shouldThrowExceptionWhenPlayerNotFound() {
        Session session = new Session();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(sessionId, playerId)
        );

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create initial teams when have enough players")
    void shouldCreateInitialTeamsWhenSessionHasEnoughPlayers() {
        Session session = new Session();

        for (int i = 1; i <= 7; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        Player p8 = new Player(playerId, "P8");

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        List<Team> teams = List.of(
                createTeam(1, 4),
                createTeam(2, 4)
        );

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.of(p8));
        when(initialTeamsBalancerService.createInitialTeams(session))
                .thenReturn(teams);

        useCase.execute(sessionId, playerId);

        verify(initialTeamsBalancerService).createInitialTeams(session);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Not create initial teams when have not enough players")
    void shouldNotCreateInitialTeamsBeforeEnoughPlayers() {
        Session session = new Session();

        for (int i = 1; i <= 2; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        Player p3 = new Player(playerId, "P3");

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.of(p3));

        useCase.execute(sessionId, playerId);

        verify(initialTeamsBalancerService, never()).createInitialTeams(any());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Not create initial teams when session already shuffled")
    void shouldNotCreateInitialTeamsWhenSessionAlreadyShuffled() {
        Session session = new Session();

        for (int i = 1; i <= 7; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        session.markAsShuffled();

        Player p8 = new Player(playerId, "P8");

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.of(p8));

        useCase.execute(sessionId, playerId);

        verify(initialTeamsBalancerService, never()).createInitialTeams(any());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Add player when session already started")
    void shouldAddPlayerAndPersistWhenSessionAlreadyStarted() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(new ArrayList<>(
                List.of(t1, t2))
        );

        session.initializeSession();

        int previousPlayers = session.getActivePlayers().size();

        Player p9 = new Player(playerId, "P9");

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.of(p9));

        useCase.execute(sessionId, playerId);

        assertEquals(
                previousPlayers + 1,
                session.getActivePlayers().size()
        );

        verify(initialTeamsBalancerService, never()).createInitialTeams(any());

        verify(sessionRepository).save(session);
    }
}