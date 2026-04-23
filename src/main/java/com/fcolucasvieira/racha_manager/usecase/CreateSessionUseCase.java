package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSessionUseCase {

    private final SessionRepository repository;

    public UUID execute() {
        Session session = new Session();

        repository.save(session);

        return session.getId();
    }
}
