package com.fcolucasvieira.racha_manager.player.usecase;

import com.fcolucasvieira.racha_manager.common.observability.BusinessMetricsService;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePlayerUseCase {

    private final PlayerRepository repository;
    private final BusinessMetricsService businessMetrics;

    private static final Logger log = LoggerFactory.getLogger(CreatePlayerUseCase.class);

    public UUID execute(String name) {
        Player player = new Player(name);

        repository.save(player);

        log.info(
                "[PLAYER_CREATED] playerId={} playerName={}",
                player.getId(),
                player.getName()
        );

        businessMetrics.incrementPlayersCreated();

        return player.getId();
    }
}
