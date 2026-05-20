package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishMatchUseCase {
    private final SessionRepository sessionRepository;
    private final PriorityService priorityService;

    private static final Logger log = LoggerFactory.getLogger(FinishMatchUseCase.class);

    public void execute(UUID sessionId, int winnerTeamNumber) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        var previousMatch = session.getCurrentMatch();

        int previousTeamA = previousMatch.getTeamA().getNumber();
        int previousTeamB = previousMatch.getTeamB().getNumber();

        session.finishMatch(winnerTeamNumber);

        log.info(
                "[MATCH_FINISHED] sessionId={} winnerTeamNumber={} finishedMatch={}vs{} nextMatch={}vs{}",
                sessionId,
                winnerTeamNumber,
                previousTeamA,
                previousTeamB,
                session.getCurrentMatch().getTeamA().getNumber(),
                session.getCurrentMatch().getTeamB().getNumber()
        );

        priorityService.apply(session);

        log.info(
                "[PRIORITY_APPLIED] sessionId={}",
                sessionId
        );

        sessionRepository.save(session);
    }
}
