package com.fcolucasvieira.racha_manager.domain.port;

import com.fcolucasvieira.racha_manager.domain.model.Session;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepositoryPort {
    Session save(Session session);
    Optional<Session> findById(UUID id);
}
