package com.fcolucasvieira.racha_manager.session.repository;

import com.fcolucasvieira.racha_manager.session.model.Session;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SessionRepository {
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public Session save(Session session){
        sessions.put(session.getId(), session);
        return session;
    };

    public Optional<Session> findById(UUID id){
        return Optional.ofNullable(sessions.get(id));
    };
}
