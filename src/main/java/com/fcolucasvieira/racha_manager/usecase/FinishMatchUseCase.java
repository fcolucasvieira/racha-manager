package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishMatchUseCase {
    private final SessionRepository sessionRepository;
    private final PriorityService priorityService;

    public void execute(UUID sessionId, int winnerTeamNumber) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.finishMatch(winnerTeamNumber);

        priorityService.apply(session);

        sessionRepository.save(session);
    }
}
