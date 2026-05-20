package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
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
class FinishMatchUseCaseTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private PriorityService priorityService;

    @InjectMocks private FinishMatchUseCase useCase;

    UUID sessionId;
    UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    // helper para criação de times
    Team createTeam(int number, int countPlayers) {
        // cria time através do parâmetro number
        Team team = new Team(number);

        // adiciona a qtde. de players no time através do parâmetro countPlayers
        for(int i = 1; i <= countPlayers; i++) {
            team.addPlayer(new PlayerEntity(UUID.randomUUID(), "P" + i));
        }

        // retorna time
        return team;
    }

    @Test
    void shouldFinishMatchSuccessfully() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        session.updateTeams(teams);

        session.startQueue();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId, 1);

        // assert
        assertEquals(1, session.getCurrentMatch().getTeamA().getNumber());
        assertEquals(3, session.getCurrentMatch().getTeamB().getNumber());

        assertEquals(1, session.getQueue().size());
        assertEquals(2, session.getQueue().get(0).getNumber());

        verify(priorityService).apply(session);
        verify(sessionRepository).save(session);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, 1));

        verify(priorityService, never()).apply(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenWinnerIsInvalid() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        session.updateTeams(teams);

        session.startQueue();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, 4));

        verify(priorityService, never()).apply(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenNoMatchInProgress() {
        Session session = new Session();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // act & assert
        assertThrows(IllegalStateException.class, () -> useCase.execute(sessionId, 1));

        verify(priorityService, never()).apply(any());
        verify(sessionRepository, never()).save(any());
    }
}