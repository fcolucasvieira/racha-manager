package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.player.repository.PlayerRepository;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.service.AddPlayerToActiveSessionService;
import com.fcolucasvieira.racha_manager.session.service.InitialTeamsBalancerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddPlayerToSessionConcurrencyTest {

    private static final int TOTAL_PLAYERS = 8;

    @Test
    @DisplayName("Should not corrupt session state when players join simultaneously")
    void shouldHandleConcurrentPlayerAdditionSafely() throws InterruptedException {
        SessionRepository sessionRepository = new SessionRepository();
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        InitialTeamsBalancerService initialTeamsBalancerService = new InitialTeamsBalancerService();
        AddPlayerToActiveSessionService addPlayerToActiveSessionService = new AddPlayerToActiveSessionService();

        AddPlayerToSessionUseCase useCase = new AddPlayerToSessionUseCase(
                sessionRepository,
                playerRepository,
                initialTeamsBalancerService,
                addPlayerToActiveSessionService
        );

        Session session = new Session();
        sessionRepository.save(session);

        List<Player> players = createPlayers(TOTAL_PLAYERS);

        for (Player player : players) {
            when(playerRepository.findById(player.getId()))
                    .thenReturn(Optional.of(player));
        }

        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_PLAYERS);

        CountDownLatch readyLatch = new CountDownLatch(TOTAL_PLAYERS);

        CountDownLatch startLatch = new CountDownLatch(1);

        CountDownLatch doneLatch = new CountDownLatch(TOTAL_PLAYERS);

        AtomicInteger unexpectedFailures = new AtomicInteger();

        for (Player player : players) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    useCase.execute(session.getId(), player.getId());
                } catch (Exception e) {

                    unexpectedFailures.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        boolean finishedInTime = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedInTime);
        assertEquals(0, unexpectedFailures.get());

        Session persisted = sessionRepository.findById(session.getId()).orElseThrow();

        assertEquals(TOTAL_PLAYERS, persisted.getActivePlayers().size());
        assertTrue(persisted.hasStarted());
        assertEquals(2, persisted.getTeams().size());

        int totalPlayersInTeams = persisted.getTeams().stream()
                .mapToInt(team -> team.getPlayers().size())
                .sum();
        assertEquals(TOTAL_PLAYERS, totalPlayersInTeams);
    }

    private List<Player> createPlayers(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new Player(UUID.randomUUID(), "P" + i))
                .toList();
    }
}