package com.fcolucasvieira.racha_manager.repository;

import com.fcolucasvieira.racha_manager.domain.model.Session;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {

    Session save(Session session);

    Optional<Session> findById(UUID id);
}
