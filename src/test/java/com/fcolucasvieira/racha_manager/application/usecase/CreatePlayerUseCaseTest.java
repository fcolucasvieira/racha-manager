package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.player.usecase.CreatePlayerUseCase;
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
        String name = "Lucas";

        when(repository.save(any(Player.class)))
                .thenAnswer(invocation -> {
                    Player player = invocation.getArgument(0);
                    player.setId(UUID.randomUUID());
                    return player;
                });

        UUID result = useCase.execute(name);

        assertNotNull(result);

        verify(repository).save(argThat(player ->
                player.getName().equals(name)
        ));
    }
}