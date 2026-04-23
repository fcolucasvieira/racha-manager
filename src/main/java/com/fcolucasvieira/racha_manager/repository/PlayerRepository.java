package com.fcolucasvieira.racha_manager.repository;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<PlayerEntity, UUID> {
    Optional<PlayerEntity> findById(UUID id);

}
