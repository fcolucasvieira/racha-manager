package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemovePlayerFromSessionUseCaseTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private PriorityService priorityService;

    @InjectMocks private RemovePlayerFromSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    // Helper
    private Team createTeam(int number, int playersCount) {
        Team team = new Team(number);

        for(int i = 1; i <= playersCount; i++) {
            team.addPlayer(
                    new PlayerEntity(
                    number == 1 && i == 1
                            ? playerId
                            : UUID.randomUUID(),
                    "P" + i
                    )
            );
        }

        return team;
    }

    @Test
    void shouldRemovePlayerFromSession() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 2);

        session.updateTeams(
                new ArrayList<>(List.of(t1))
        );

        // adiciona jogadores a sessão
        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId, playerId);

        // assert
        assertEquals(1, session.getActivePlayers().size());
        assertEquals(1, t1.getPlayers().size());

        verify(sessionRepository).save(session);
    }

    @Test
    void shouldDissolveEmptyTeam() {
        // arrange
        Session session = new Session();

        // t1 com um jogador (playerId, P1)
        Team t1 = createTeam(1, 1);

        // atualiza times da sessão
        session.updateTeams(
                new ArrayList<>(List.of(t1))
        );

        // aos jogadores de t1, incrementa na sessão (activePlayers)
        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        //act
        useCase.execute(sessionId, playerId);

        assertTrue(session.getTeams().isEmpty());
    }

    @Test
    void shouldApplyPriorityServiceWhenSessionHasStarted() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 4);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 2);

        // Incrementa times na sessão
        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        // adiciona players na sessão (activePlayers)
        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        // inicia queue para que o time com jogador removido, seja incrementado os 4 jogadores
        session.startQueue();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        UUID removablePlayerId = t3.getPlayers().get(0).getId();

        // act
        useCase.execute(sessionId, removablePlayerId);

        // assert
        assertTrue(t1.isFull());

        assertEquals(1, session.getQueue().size());
        assertEquals(1, t3.getPlayers().size());

        verify(priorityService).apply(session);
        verify(sessionRepository).save(session);
    }

    @Test
    void shouldNotApplyPriorityServiceWhenSessionHasNotStarted() {
        // arrange
        Session session = new Session();

        Team t1 = createTeam(1, 2);

        session.updateTeams(
                new ArrayList<>(List.of(t1))
        );

        t1.getPlayers().forEach(session::addPlayer);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // act
        useCase.execute(sessionId, playerId);

        // assert
        verify(priorityService, never()).apply(any());
    }

    @Test
    void shouldThrowExceptionWhenRemovingLastPlayerFromCurrentMatchTeam() {
        // arrange
        Session session = new Session();

        // t1 em currentMatch e com jogador a ser removido -> deve lançar excessão
        Team t1 = createTeam(1, 1);
        Team t2 = createTeam(2, 4);
        Team t3 = createTeam(3, 4);


        session.updateTeams(
                new ArrayList<>(List.of(t1, t2, t3))
        );

        session.getTeams().forEach(
                team -> team.getPlayers().forEach(session::addPlayer)
        );

        // inicia queue para que currentMatch (t1 vs t2) esteja ocorrendo
        session.startQueue();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // act + assert
        assertThrows(IllegalStateException.class, () -> useCase.execute(sessionId, playerId));

        verify(priorityService, never()).apply(any());
        verify(sessionRepository, never()).save(session);
    }
}