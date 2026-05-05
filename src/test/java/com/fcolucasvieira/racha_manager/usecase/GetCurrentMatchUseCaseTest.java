package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
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
class GetCurrentMatchUseCaseTest {

    @Mock
    private SessionRepository repository;

    @InjectMocks
    private GetCurrentMatchUseCase useCase;

    @Test
    void shouldReturnCurrentMatchSuccessfully() {
        // arrange
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();

        Team t1 = new Team(1);
        Team t2 = new Team(2);
        Team t3 = new Team(3);

        session.updateTeams(List.of(t1, t2, t3));
        session.startQueue();

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        // act
        Match result = useCase.execute(sessionId);

        // assert
        assertNotNull(result);
        assertEquals(1, result.getTeamA().getNumber());
        assertEquals(2, result.getTeamB().getNumber());

        verify(repository).findById(sessionId);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();

        when(repository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(sessionId));
    }

    @Test
    void shouldThrowExceptionWhenMatchNotStarted() {
        UUID sessionId = UUID.randomUUID();

        Session session = new Session();

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(sessionId));
    }
}