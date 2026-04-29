package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.TeamBalancerService;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
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
class AddPlayerToSessionUseCaseTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamBalancerService teamBalancerService;

    @InjectMocks
    private  AddPlayerToSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    @Test
    void shouldAddPlayerToSessionSuccessfully() {
        // arrange
        Session session = spy(new Session());
        PlayerEntity player = new PlayerEntity(playerId, "Lucas");

        List<Team> balancedTeams = List.of(new Team(1));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(teamBalancerService.balance(session)).thenReturn(balancedTeams);

        // act
        List<Team> result = useCase.execute(sessionId, playerId);

        // assert & verify
        assertEquals(balancedTeams, result);

        verify(session).addPlayer(player);
        verify(teamBalancerService).balance(session);
        verify(session).updateTeams(balancedTeams);
        verify(sessionRepository).save(session);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // assert, verify & act
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));

        verifyNoInteractions(playerRepository, teamBalancerService);
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotFound() {
        // arrange
        Session session = new Session();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // assert, verify & act
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));

        verify(sessionRepository).findById(sessionId);
        verify(playerRepository).findById(playerId);
        verifyNoInteractions(teamBalancerService);
    }

    }