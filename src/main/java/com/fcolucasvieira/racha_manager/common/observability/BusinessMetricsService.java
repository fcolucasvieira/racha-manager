package com.fcolucasvieira.racha_manager.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessMetricsService {
    private final MeterRegistry meterRegistry;

    private Counter sessionsCreated;
    private Counter playersCreated;

    @PostConstruct
    void init() {
        sessionsCreated = Counter.builder("racha_sessions_created_total")
                .description("Total sessions created")
                .register(meterRegistry);

        playersCreated = Counter.builder("racha_players_created_total")
                .description("Total players created")
                .register(meterRegistry);
    }

    public void incrementSessionsCreated() {
        sessionsCreated.increment();
    }

    public void incrementPlayersCreated() {
        playersCreated.increment();
    }
}
