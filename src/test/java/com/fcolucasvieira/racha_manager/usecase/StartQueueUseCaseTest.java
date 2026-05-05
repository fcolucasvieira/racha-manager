package com.fcolucasvieira.racha_manager.usecase;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartQueueUseCaseTest {

    @Mock
    private SessionRepository repository;

    @InjectMocks
    private StartQueueUseCase useCase;

    private UUID sessionId;

    private Session session;

    @BeforeEach
    void setup () {
        sessionId = UUID.randomUUID();
        session = new Session();
    }


    @Test
    void shouldStartQueueSuccessfully() {
        // arrange
        Team t1 = new Team(1);
        Team t2 = new Team(2);
        Team t3 = new Team(3);

        session.updateTeams(List.of(t1, t2, t3));

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId);

        // assert
        assertNotNull(session.getCurrentMatch());
        assertEquals(t1, session.getCurrentMatch().getTeamA());
        assertEquals(t2, session.getCurrentMatch().getTeamB());

        assertEquals(1, session.getQueue().size());
        assertEquals(t3, session.getQueue().getFirst());

        verify(repository).save(session);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotExists() {
        // arrange
        when(repository.findById(sessionId)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId));
    }

    @Test
    void shouldThrowExceptionWhenLessThanTwoTeams() {
        // arrange
        session.updateTeams(List.of(new Team(1)));

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        // act & assert
        assertThrows(IllegalStateException.class, () -> useCase.execute(sessionId));
    }

    @Test
    void shouldThrowExceptionWhenQueueAlreadyStarted() {
        UUID sessionId = UUID.randomUUID();

        Session session = new Session();

        Team t1 = new Team(1);
        Team t2 = new Team(2);

        session.updateTeams(List.of(t1, t2));
        session.startQueue();

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(sessionId));
    }
}