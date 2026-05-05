package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
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
class FinishMatchUseCaseTest {

    @Mock
    private SessionRepository repository;

    @InjectMocks
    private FinishMatchUseCase useCase;

    @Test
    void shouldRotateQueueCorrectlyWhenFinishingMatch() {
        // arrange
        UUID sessionId = UUID.randomUUID();

        Session session = new Session();

        Team t1 = new Team(1);
        Team t2 = new Team(2);
        Team t3 = new Team(3);

        session.updateTeams(List.of(t1, t2, t3));
        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId, 1);

        // assert
        Match match = session.getCurrentMatch();

        assertEquals(1, match.getTeamA().getNumber());
        assertEquals(3, match.getTeamB().getNumber());

        List<Team> queue = session.getQueue();
        assertEquals(1, queue.size());
        assertEquals(2, queue.getFirst().getNumber());

        verify(repository).save(session);
    }

    @Test
    void shouldKeepCorrectOrderWithFourTeams() {
        UUID sessionId = UUID.randomUUID();

        Session session = new Session();

        Team t1 = new Team(1);
        Team t2 = new Team(2);
        Team t3 = new Team(3);
        Team t4 = new Team(4);

        session.updateTeams(List.of(t1, t2, t3, t4));
        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(sessionId, 1);

        Match match = session.getCurrentMatch();

        assertEquals(1, match.getTeamA().getNumber());
        assertEquals(3, match.getTeamB().getNumber());

        List<Team> queue = session.getQueue();

        assertEquals(List.of(t4, t2), queue);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();

        when(repository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(sessionId, 1));
    }

    @Test
    void shouldThrowExceptionWhenNoMatchInProgress() {
        UUID sessionId = UUID.randomUUID();

        Session session = new Session();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(sessionId, 1));
    }

    @Test
    void shouldKeepSameTeamsWhenOnlyTwoTeamsExist() {
        UUID sessionId = UUID.randomUUID();

        Session session = new Session();

        Team t1 = new Team(1);
        Team t2 = new Team(2);

        session.updateTeams(List.of(t1, t2));
        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        useCase.execute(sessionId, 1);

        Match match = session.getCurrentMatch();

        assertEquals(1, match.getTeamA().getNumber());
        assertEquals(2, match.getTeamB().getNumber());
    }
}