package com.fcolucasvieira.racha_manager.infraestructure.persistence.adapter;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.port.PlayerRepositoryPort;
import com.fcolucasvieira.racha_manager.infraestructure.persistence.jpa.SpringDataPlayerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PlayerRepositoryAdapter
        implements PlayerRepositoryPort {
    private final SpringDataPlayerRepository repository;

    public  PlayerRepositoryAdapter(SpringDataPlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public PlayerEntity save(PlayerEntity player) {
        return repository.save(player);
    }

    @Override
    public Optional<PlayerEntity> findById(UUID id){
        return repository.findById(id);
    }
}
