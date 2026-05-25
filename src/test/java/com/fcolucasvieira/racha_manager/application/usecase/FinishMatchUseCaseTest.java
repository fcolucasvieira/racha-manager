package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.enums.MatchResultType;
import com.fcolucasvieira.racha_manager.domain.model.*;
import com.fcolucasvieira.racha_manager.domain.service.MatchFlowService;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
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

    @Mock private SessionRepositoryPort sessionRepositoryPort;
    @Mock private PriorityService priorityService;
    @Mock private MatchFlowService matchFlowService;

    @InjectMocks
    private FinishMatchUseCase useCase;

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
        for (int i = 1; i <= countPlayers; i++) {
            team.addPlayer(new PlayerEntity(UUID.randomUUID(), "P" + i));
        }

        // retorna time
        return team;
    }

    @Test
    void shouldFinishMatchWithWinnerSuccessfully() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3));

        session.updateTeams(teams);

        session.startQueue();

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId, 1, MatchResultType.WINNER);

        // assert
        verify(matchFlowService)
                .finishWithWinner(session, 1);

        verify(priorityService)
                .apply(session);

        verify(sessionRepositoryPort)
                .save(session);
    }

    @Test
    void shouldFinishMatchWithDrawSuccessfully() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 4);

        List<Team> teams = new ArrayList<>(List.of(t1, t2, t3, t4));

        session.updateTeams(teams);

        session.startQueue();

        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId, null, MatchResultType.DRAW);

        // assert
        verify(matchFlowService)
                .finishWithDraw(session);

        verify(priorityService)
                .apply(session);

        verify(sessionRepositoryPort)
                .save(session);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepositoryPort.findById(sessionId))
                .thenReturn(Optional.empty());

        // act & assert
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(
                        sessionId,
                        1,
                        MatchResultType.WINNER)
        );

        verify(matchFlowService, never())
                .finishWithWinner(any(), anyInt());

        verify(priorityService, never())
                .apply(any());

        verify(sessionRepositoryPort, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenWinnerTypeHasNullWinner() {
        // act & assert
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(
                        sessionId,
                        null,
                        MatchResultType.WINNER)
        );

        verify(matchFlowService, never())
                .finishWithWinner(any(), anyInt());

        verify(priorityService, never())
                .apply(any());

        verify(sessionRepositoryPort, never())
                .save(any());
    }


    @Test
    void shouldThrowExceptionWhenDrawHasWinnerNumber() {
        // act & assert
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        sessionId,
                        1,
                        MatchResultType.DRAW
                )
        );

        verify(matchFlowService, never())
                .finishWithDraw(any());

        verify(priorityService, never())
                .apply(any());

        verify(sessionRepositoryPort, never())
                .save(any());
    }
}