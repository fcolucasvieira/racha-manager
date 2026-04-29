package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
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
class GetActivePlayersUseCaseTest {

    @Mock
    private SessionRepository repository;

    @InjectMocks
    private GetActivePlayersUseCase useCase;

    @Test
    void shouldReturnActivePlayersWhenSessionExists() {
        // arrange
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();

        UUID playerId = UUID.randomUUID();
        session.addPlayer(
                new PlayerEntity(playerId, "Lucas")
        );

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        // act
        List<PlayerEntity> result = useCase.execute(sessionId);

        // assert & verify
        assertAll(
                () ->  assertEquals(1, result.size()),
                () -> assertEquals(playerId, result.getFirst().getId()),
                () -> assertEquals("Lucas", result.getFirst().getName())
        );

        verify(repository).findById(sessionId);
    }

    @Test
    void shouldReturnEmptyListWhenNoActivePlayers() {
        // arrange
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();

        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        // act
        List<PlayerEntity> result = useCase.execute(sessionId);

        // assert & verify
        assertTrue(result.isEmpty());

        verify(repository).findById(sessionId);
    }


        @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // arrange
        UUID sessionId = UUID.randomUUID();

        when(repository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId));
    }
}