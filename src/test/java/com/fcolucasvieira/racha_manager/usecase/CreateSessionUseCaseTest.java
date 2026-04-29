package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
        // act
        UUID result = useCase.execute();

        // assert & verify
        assertNotNull(result);
        verify(repository).save(any(Session.class));
    }
}