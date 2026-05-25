package com.fcolucasvieira.racha_manager.domain.port;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepositoryPort {
    PlayerEntity save(PlayerEntity player);
    Optional<PlayerEntity> findById(UUID id);
}
