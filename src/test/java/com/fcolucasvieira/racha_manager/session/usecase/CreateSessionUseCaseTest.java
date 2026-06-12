package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSessionUseCaseTest {
    @Mock
    private SessionRepository repository;

    @InjectMocks
    private CreateSessionUseCase useCase;

    @Test
    void shouldCreateSessionSuccessfully() {
        UUID result = useCase.execute();

        assertNotNull(result);
        verify(repository).save(any(Session.class));
    }
}