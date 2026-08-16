package com.fcolucasvieira.racha_manager.session.usecase;

import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.repository.SessionRepository;
import com.fcolucasvieira.racha_manager.session.service.TeamCompletionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemovePlayerFromSessionConcurrencyTest {

    @Test
    @DisplayName("Should not corrupt state session when players leave simultaneously")
    void shouldHandleConcurrentPlayerRemovalSafely() throws InterruptedException {
        SessionRepository sessionRepository = new SessionRepository();
        TeamCompletionService teamCompletionService = new TeamCompletionService();

        RemovePlayerFromSessionUseCase useCase = new RemovePlayerFromSessionUseCase(
                sessionRepository,
                teamCompletionService
        );

        Session session = new Session();
        List<Player> allPlayers = new ArrayList<>();

        Team team1 = new Team(1);
        Team team2 = new Team(2);

        for (int i = 1; i <= 4; i++) {
            Player p = new Player(UUID.randomUUID(), "TimeA-P" + i);
            allPlayers.add(p);
            session.addPlayer(p);
            team1.addPlayer(p);
        }
        for (int i = 1; i <= 4; i++) {
            Player p = new Player(UUID.randomUUID(), "TimeB-P" + i);
            allPlayers.add(p);
            session.addPlayer(p);
            team2.addPlayer(p);
        }

        session.setTeams(new ArrayList<>(List.of(team1, team2)));
        session.initializeSession();

        sessionRepository.save(session);

        List<Player> playersToRemove = List.of(
                team1.getPlayers().get(0),
                team1.getPlayers().get(1),
                team2.getPlayers().get(0),
                team2.getPlayers().get(1)
        );

        int totalThreads = playersToRemove.size();
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch readyLatch = new CountDownLatch(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        AtomicInteger unexpectedFailures = new AtomicInteger();

        for (Player player : playersToRemove) {
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

        assertEquals(allPlayers.size() - playersToRemove.size(), persisted.getActivePlayers().size());
        assertEquals(2, persisted.getTeams().size());

        int totalPlayersInTeams = persisted.getTeams().stream()
                .mapToInt(team -> team.getPlayers().size())
                .sum();
        assertEquals(4, totalPlayersInTeams);
    }
}