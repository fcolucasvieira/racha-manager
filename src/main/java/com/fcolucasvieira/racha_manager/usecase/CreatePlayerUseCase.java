package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePlayerUseCase {
    private final PlayerRepository playerRepository;

    public UUID execute(String name) {
        PlayerEntity player = PlayerEntity.builder()
                .name(name)
                .build();

        playerRepository.save(player);

        return player.getId();
    }
}
