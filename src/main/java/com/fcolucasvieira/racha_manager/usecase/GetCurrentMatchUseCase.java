package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentMatchUseCase {
    private final SessionRepository repository;

    public Match execute(UUID sessionId) {
        Session session = repository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        return  session.getCurrentMatch();
    }
}
