package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePlayerUseCaseTest {

    @Mock
    private PlayerRepository repository;

    @InjectMocks
    private CreatePlayerUseCase useCase;

    @Test
    void shouldCreatePlayerSuccessfully() {
        // arrange
        String name = "Lucas";

        when(repository.save(any(PlayerEntity.class)))
                .thenAnswer(invocation -> {
                    PlayerEntity player = invocation.getArgument(0);
                    player.setId(UUID.randomUUID());
                    return player;
                });

        // act
        UUID result = useCase.execute(name);

        // assert & verify
        assertNotNull(result);

        verify(repository).save(argThat(player ->
                player.getName().equals(name)
        ));
    }
}