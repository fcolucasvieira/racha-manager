package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.session.enums.MatchResultType;
import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.service.MatchFlowService;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.model.Session;
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
    private SessionRepository sessionRepository;
    @Mock
    private MatchFlowService matchFlowService;

    @InjectMocks
    private FinishMatchUseCase finishMatchUseCase;

    private UUID sessionId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
    }

    // helper (criação de time)
    private Team createTeam(int number, int countPlayers) {
        Team team = new Team(number);

        for (int i = 1; i <= countPlayers; i++) {
            team.addPlayer(
                    new Player(UUID.randomUUID(), "P" + i)
            );
        }

        return team;
    }

    @Test
    @DisplayName("Should finish match with winner")
    void shouldFinishMatchWithWinnerSuccessfully() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 1);

        session.setTeams(
                List.of(t1, t2, t3)
        );

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        finishMatchUseCase.execute(sessionId, 1, MatchResultType.WINNER);

        verify(matchFlowService).finishWithWinner(session, 1);
        verify(matchFlowService, never()).finishWithDraw(any());

        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should finish match with draw")
    void shouldFinishMatchWithDrawSuccessfully() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);
        Team t4 = createTeam(4, 4);

        session.setTeams(
                List.of(t1, t2, t3, t4));

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        finishMatchUseCase.execute(sessionId, null, MatchResultType.DRAW);

        verify(matchFlowService).finishWithDraw(session);
        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());

        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should throw exception when session does not exist")
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> finishMatchUseCase.execute(sessionId, 1, MatchResultType.WINNER)
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when session has not started")
    void shouldThrowExceptionWhenSessionHasNotStarted() {
        Session session = new Session();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                ConflictException.class,
                () -> finishMatchUseCase.execute(sessionId, 1, MatchResultType.WINNER)
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when winner result has null winner number team")
    void shouldThrowExceptionWhenWinnerTypeHasNullWinner() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(List.of(t1, t2));

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                ValidationException.class,
                () -> finishMatchUseCase.execute(sessionId, null, MatchResultType.WINNER)
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when draw contains winner number team")
    void shouldThrowExceptionWhenDrawHasWinnerNumber() {
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);

        session.setTeams(List.of(t1, t2));

        session.initializeSession();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(
                ValidationException.class,
                () -> finishMatchUseCase.execute(sessionId, 1, MatchResultType.DRAW)
        );

        verify(matchFlowService, never()).finishWithWinner(any(), anyInt());
        verify(matchFlowService, never()).finishWithDraw(any());
        verify(sessionRepository, never()).save(any());
    }
}