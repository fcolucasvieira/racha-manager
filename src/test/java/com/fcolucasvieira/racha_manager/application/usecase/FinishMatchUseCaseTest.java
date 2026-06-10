package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.session.enums.MatchResultType;
import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.service.MatchFlowService;
import com.fcolucasvieira.racha_manager.session.service.PriorityService;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.usecase.FinishMatchUseCase;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinishMatchUseCaseTest {
    @Mock
    private SessionRepository repository;
    @Mock
    private PriorityService priorityService;
    @Mock
    private MatchFlowService matchFlowService;

    @InjectMocks
    private FinishMatchUseCase useCase;

    private UUID sessionId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
    }

    // helper
    private Team createTeam(int number, int countPlayers) {
        Team team = new Team(number);

        for (int i = 1; i <= countPlayers; i++) {
            team.addPlayer(
                    new Player(
                            UUID.randomUUID(),
                            "P" + i
                    )
            );
        }

        return team;
    }

    @Test
    @DisplayName("Success (Winner)")
    void shouldFinishMatchWithWinnerSuccessfully() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);

        session.setTeams(new ArrayList<>(List.of(t1, t2, t3)));

        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(
                sessionId,
                1,
                MatchResultType.WINNER
        );

        verify(matchFlowService).finishWithWinner(session, 1);
        verify(priorityService).apply(session);
        verify(repository).save(session);
    }

    @Test
    @DisplayName("Success (Draw)")
    void shouldFinishMatchWithDrawSuccessfully() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 4);

        session.setTeams(new ArrayList<>(List.of(t1, t2, t3, t4)));

        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(
                sessionId,
                null,
                MatchResultType.DRAW
        );

        verify(matchFlowService).finishWithDraw(session);
        verify(priorityService, never()).apply(any());
        verify(repository).save(session);

    }

    @Test
    @DisplayName("Session not found")
    void shouldThrowExceptionWhenSessionNotFound() {
        when(repository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> useCase.execute(
                        sessionId,
                        1,
                        MatchResultType.WINNER
                )
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(priorityService, never()).apply(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Session not started")
    void shouldThrowExceptionWhenSessionHasNotStarted() {
        Session session = new Session();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                ConflictException.class,
                () -> useCase.execute(
                        sessionId,
                        1,
                        MatchResultType.WINNER
                )
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(priorityService, never()).apply(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Winner number team is null")
    void shouldThrowExceptionWhenWinnerTypeHasNullWinner() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(new ArrayList<>(List.of(t1, t2)));

        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                ValidationException.class,
                () -> useCase.execute(
                        sessionId,
                        null,
                        MatchResultType.WINNER
                )
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(priorityService, never()).apply(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Draw contains winner number team")
    void shouldThrowExceptionWhenDrawHasWinnerNumber() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(new ArrayList<>(List.of(t1, t2)));

        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                ValidationException.class,
                () -> useCase.execute(
                        sessionId,
                        1,
                        MatchResultType.DRAW
                )
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(priorityService, never()).apply(any());
        verify(repository, never()).save(any());
    }
}