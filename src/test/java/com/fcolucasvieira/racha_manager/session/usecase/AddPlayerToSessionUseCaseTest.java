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
    private AddPlayerToSessionUseCase addPlayerToSessionUseCase;

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
                () -> addPlayerToSessionUseCase.execute(sessionId, playerId)
        );

        verify(playerRepository, never()).findById(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when player does not exist")
    void shouldThrowExceptionWhenPlayerNotFound() {
        Session session = new Session();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> addPlayerToSessionUseCase.execute(sessionId, playerId)
        );

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create initial teams when eight players joins")
    void shouldCreateInitialTeamsWhenSessionHasEnoughPlayers() {
        Session session = new Session();

        for (int i = 1; i <= 7; i++) {
            session.addPlayer(createPlayer("P" + i));
        }

        Player p8 = new Player(playerId, "P8");

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

        addPlayerToSessionUseCase.execute(sessionId, playerId);

        assertTrue(session.hasStarted());

        verify(initialTeamsBalancerService).createInitialTeams(session);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should not create initial teams before minimum players")
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

        addPlayerToSessionUseCase.execute(sessionId, playerId);

        verify(initialTeamsBalancerService, never()).createInitialTeams(any());
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should not create initial teams when session already shuffled")
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

        addPlayerToSessionUseCase.execute(sessionId, playerId);

        verify(initialTeamsBalancerService, never()).createInitialTeams(any());
        verify(sessionRepository).save(session);
    }

    // TODO: Adicionar testes p/ casos de entrada de jogador com a sessão acontecendo
}