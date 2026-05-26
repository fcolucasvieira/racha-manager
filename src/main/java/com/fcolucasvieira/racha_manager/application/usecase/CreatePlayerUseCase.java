package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.port.PlayerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePlayerUseCase {

    private final PlayerRepositoryPort repository;

    private static final Logger log = LoggerFactory.getLogger(CreatePlayerUseCase.class);

    public UUID execute(String name) {
        PlayerEntity player = new PlayerEntity(name);

        log.info(
                "[PLAYER_CREATED] playerId={} playerName={}",
                player.getId(),
                player.getName()
        );

        repository.save(player);

        return player.getId();
    }
}
