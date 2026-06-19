package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.common.observability.BusinessMetricsService;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSessionUseCase {

    private final SessionRepository repository;
    private final BusinessMetricsService businessMetrics;

    private static final Logger log = LoggerFactory.getLogger(CreateSessionUseCase.class);

    public UUID execute() {
        Session session = new Session();

        repository.save(session);

        log.info(
                "[SESSION_CREATED] sessionId={}",
                session.getId()
        );

        businessMetrics.incrementSessionsCreated();

        return session.getId();
    }
}
