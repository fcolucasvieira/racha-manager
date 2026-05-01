package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishMatchUseCase {
    private final SessionRepository repository;

    public void execute(UUID sessionId, int winnerTeamNumber) {
        Session session = repository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.finishMatch(winnerTeamNumber);

        repository.save(session);
    }
}
