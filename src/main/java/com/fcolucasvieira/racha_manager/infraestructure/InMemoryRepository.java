package com.fcolucasvieira.racha_manager.infraestructure;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRepository implements SessionRepository {

    // Uso de ConcurrentHashMap em busca de segurança e eficiência em requisições simultâneas
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public Session save(Session session) {
        sessions.put(session.getId(), session);
        return session;
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return Optional.ofNullable(sessions.get(id));
    }
}
