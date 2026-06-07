package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
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
    private SessionRepositoryPort repository;
    @InjectMocks
    private CreateSessionUseCase useCase;

    @Test
    void shouldCreateSessionSuccessfully() {
        UUID result = useCase.execute();

        assertNotNull(result);
        verify(repository).save(any(Session.class));
    }
}