package com.fcolucasvieira.racha_manager.usecase;

import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.domain.service.InitialTeamBalancerService;
import com.fcolucasvieira.racha_manager.domain.service.PriorityService;
import com.fcolucasvieira.racha_manager.repository.PlayerRepository;
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
class AddPlayerToSessionUseCaseTest {
    @Mock private SessionRepository sessionRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PriorityService priorityService;
    @Mock private InitialTeamBalancerService initialTeamBalancerService;

    @InjectMocks private AddPlayerToSessionUseCase useCase;

    private UUID sessionId;
    private UUID playerId;

    @BeforeEach
    void setup() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        // Quando buscar pelo ID, não encontrar session
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));
    }

    @Test
    void shouldThrowExceptionWhenPlayerNotFound() {
        Session session = new Session();

        // Quando buscar pelo ID, encontrar session
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Quando buscar pelo ID, não encontrar player
        when(playerRepository.findById(playerId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(sessionId, playerId));
    }


    @Test
    void shouldCreateTeamsWhenSessionReachEightPlayers() {
        Session session = new Session();

        // Adicionar 7 jogadores a uma sessão
        for(int i = 1; i <= 7; i++){
            session.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i)
            );
        }

        // 8° jogador para entrar na sessão
        PlayerEntity p8 = new PlayerEntity(playerId, "P8");

        Team t1 = new Team(1);
        Team t2 = new Team(2);

        // Como InitialTeamsBalancerService é um Mock, devemos gerar o retorno do método
        List<Team> teams = List.of(t1, t2);

        // Quando buscar pelo ID, encontrar session
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Quando buscar pelo ID, encontrar p8
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(p8));

        // Quando initialTeamsBalancerService for chamado, retornar teams
        when(initialTeamBalancerService.createInitialTeams(session)).thenReturn(teams);

        List<Team> result = useCase.execute(sessionId, playerId);

        assertEquals(2, result.size());

        verify(initialTeamBalancerService).createInitialTeams(session);

        verify(sessionRepository).save(session);
    }

    @Test
    void shouldNotCreateInitialTeamsBeforeEightPlayers() {
        Session session = new Session();

        for (int i = 1; i <= 6; i++) {
            session.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "P" + i)
            );
        }

        PlayerEntity p7 =
                new PlayerEntity(playerId, "P7");

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepository.findById(playerId))
                .thenReturn(Optional.of(p7));

        useCase.execute(sessionId, playerId);

        verify(initialTeamBalancerService, never())
                .createInitialTeams(any());

        verify(sessionRepository).save(session);
    }

    @Test
    void shouldApplyPriorityServiceWhenSessionHasStarted() {
        Session session = new Session();

        // cria 2 times completos
        Team t1 = new Team(1);
        Team t2 = new Team(2);

        for (int i = 1; i <= 4; i++) {
            t1.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "T1-P" + i)
            );

            t2.addPlayer(
                    new PlayerEntity(UUID.randomUUID(), "T2-P" + i)
            );
        }

        session.updateTeams(new ArrayList<>(List.of(t1, t2)));

        // inicia fila/currentMatch
        session.startQueue();

        PlayerEntity p9 =
                new PlayerEntity(playerId, "P9");

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(playerRepository.findById(playerId))
                .thenReturn(Optional.of(p9));

        useCase.execute(sessionId, playerId);

        verify(priorityService).apply(session);

        verify(sessionRepository).save(session);
    }
}