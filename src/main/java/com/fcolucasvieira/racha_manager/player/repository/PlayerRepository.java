package com.fcolucasvieira.racha_manager.player.repository;

import com.fcolucasvieira.racha_manager.player.model.Player;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {}
