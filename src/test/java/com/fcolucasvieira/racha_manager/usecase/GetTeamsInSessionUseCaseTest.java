package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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
class GetTeamsInSessionUseCaseTest {

    @Mock
    private SessionRepository repository;

    @InjectMocks
    private GetTeamsInSessionUseCase useCase;

    @Test
    void successfully() {
        // arrange
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.updateTeams(List.of(new Team(1)));

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        // act
        List<Team> result = useCase.execute(sessionId);

        // assert & verify
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getNumber());

        verify(repository).findById(sessionId);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();

        when(repository.findById(sessionId)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId));
    }
}