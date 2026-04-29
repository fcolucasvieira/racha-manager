package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.TeamBalancerService;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
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
    private SessionRepository sessionRepository;

    @Mock
    private TeamBalancerService teamBalancerService;

    @InjectMocks
    private RemovePlayerFromSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    @Test
    void shouldRemovePlayerFromSessionWhenSessionExists() {
        // arrange
        PlayerEntity player = new PlayerEntity(playerId, "Lucas");

        Session session = spy(new Session());
        session.addPlayer(player);

        List<Team> balancedTeams = List.of(new Team(1));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(teamBalancerService.balance(session)).thenReturn(balancedTeams);

        // act
        List<Team> result = useCase.execute(sessionId, playerId);

        // assert & verify
        assertEquals(balancedTeams, result);

        verify(sessionRepository).findById(sessionId);
        verify(session).removePlayer(playerId);
        verify(teamBalancerService).balance(session);
        verify(session).updateTeams(balancedTeams);
        verify(sessionRepository).save(session);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // act, assert & verify
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));

        verifyNoMoreInteractions(teamBalancerService);
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotInSession() {
        // arrange
        Session session = spy(new Session());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // act, assert & verify
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));

        verify(sessionRepository).findById(sessionId);
        verify(session).removePlayer(playerId);

        verifyNoInteractions(teamBalancerService);
    }
}