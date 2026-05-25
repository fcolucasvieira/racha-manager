package com.fcolucasvieira.racha_manager.infraestructure.persistence.jpa;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataPlayerRepository
        extends JpaRepository<PlayerEntity, UUID> {
}
