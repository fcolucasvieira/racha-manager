package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.session.enums.MatchResultType;
import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.service.MatchFlowService;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishMatchUseCase {
    private final SessionRepository sessionRepository;
    private final MatchFlowService matchFlowService;

    private static final Logger log = LoggerFactory.getLogger(FinishMatchUseCase.class);

    public void execute(UUID sessionId, Integer winnerTeamNumber, MatchResultType resultType) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found with Id: "  + sessionId));

        // Validação duplicada?
        // Em matchFlowService.finish<Winner/Draw>, há uma validação semelhante, diferenciando apenas pela mensagem
        if (!session.hasStarted())
            throw new ConflictException("Session has not started");

        validateResultConsistency(winnerTeamNumber, resultType);

        // estado anterior (observability code)
        var previousMatch = session.getCurrentMatch();

        int previousTeamA = previousMatch.getTeamA().getNumber();
        int previousTeamB = previousMatch.getTeamB().getNumber();

        switch (resultType) {
            case DRAW -> {
                matchFlowService.finishWithDraw(session);

                log.info(
                        "[MATCH_DRAW_FINISHED] sessionId={} finishedMatch={}vs{} nextMatch={}vs{}",
                        sessionId,
                        previousTeamA,
                        previousTeamB,
                        session.getCurrentMatch().getTeamA().getNumber(),
                        session.getCurrentMatch().getTeamB().getNumber()
                );
            }
            case WINNER -> {
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
            }

            default -> throw new ValidationException("Unsupported match result type: " + resultType);
        }

        sessionRepository.save(session);
    }

    private void validateResultConsistency(Integer winnerTeamNumber, MatchResultType resultType) {
        if(resultType == MatchResultType.WINNER && winnerTeamNumber == null)
            throw new ValidationException("Winner team number is required");

        if(resultType == MatchResultType.DRAW && winnerTeamNumber != null)
            throw new ValidationException("Winner team number must be null on draw");
    }
}
