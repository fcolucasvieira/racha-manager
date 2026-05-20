package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSessionUseCase {

    private final SessionRepository repository;

    private static final Logger log = LoggerFactory.getLogger(CreateSessionUseCase.class);

    public UUID execute() {
        Session session = new Session();

        log.info(
                "[SESSION_CREATED] sessionId={}",
                session.getId()
        );

        repository.save(session);

        return session.getId();
    }
}
