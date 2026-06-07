package com.fcolucasvieira.racha_manager.application.usecase;

import com.fcolucasvieira.racha_manager.domain.enums.MatchResultType;
import com.fcolucasvieira.racha_manager.domain.exception.ConflictException;
import com.fcolucasvieira.racha_manager.domain.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.domain.exception.ValidationException;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.service.MatchFlowService;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.domain.port.SessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishMatchUseCase {
    private final SessionRepositoryPort sessionRepositoryPort;
    private final PriorityService priorityService;
    private final MatchFlowService matchFlowService;

    private static final Logger log = LoggerFactory.getLogger(FinishMatchUseCase.class);

    public void execute(UUID sessionId,
                        Integer winnerTeamNumber,
                        MatchResultType resultType) {

        Session session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: "  + sessionId));

        if (!session.hasStarted()) {
            throw new ConflictException("Session has not started");
        }

        validateResultConsistency(winnerTeamNumber, resultType);

        // guarda estado anterior (observabilidade de código)
        var previousMatch = session.getCurrentMatch();

        int previousTeamA = previousMatch.getTeamA().getNumber();
        int previousTeamB = previousMatch.getTeamB().getNumber();

        if(resultType == MatchResultType.DRAW) {

            matchFlowService.finishWithDraw(session);

            log.info(
                    "[MATCH_DRAW_FINISHED] sessionId={} finishedMatch={}vs{} nextMatch={}vs{}",
                    sessionId,
                    previousTeamA,
                    previousTeamB,
                    session.getCurrentMatch().getTeamA().getNumber(),
                    session.getCurrentMatch().getTeamB().getNumber()
            );
        } else {

            matchFlowService.finishWithWinner(session, winnerTeamNumber);

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
        }

        sessionRepositoryPort.save(session);
    }

    private void validateResultConsistency(Integer winnerTeamNumber, MatchResultType resultType) {
        if(resultType == MatchResultType.WINNER && winnerTeamNumber == null) {
            throw new ValidationException("Winner team number is required");
        }

        if(resultType == MatchResultType.DRAW && winnerTeamNumber != null) {
            throw new ValidationException("Winner team number must be null on draw");
        }
    }
}
