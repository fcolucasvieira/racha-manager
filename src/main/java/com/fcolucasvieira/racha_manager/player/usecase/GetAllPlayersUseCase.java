package com.fcolucasvieira.racha_manager.player.usecase;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class GetAllPlayersUseCase {
    private final PlayerRepository repository;

    public Page<Player> execute(UUID userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }
}
