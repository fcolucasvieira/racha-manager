package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.constant.RachaRules.INITIAL_TEAMS;

@Getter
public class Session {
    private final UUID id;

    private final List<Player> activePlayers;

    private List<Team> teams;

    private WaitingQueue waitingQueue;

    private Match currentMatch;

    // Nome de atributo melhor? (trazer + clareza)
    private boolean shuffled;

    public Session() {
        this.id = UUID.randomUUID();

        this.activePlayers = new ArrayList<>();

        // (Teams) Iniciar apenas quando houver da qtde. mínima de jogadores ativos p/ iniciar sessão?
        this.teams = new ArrayList<>();

        this.waitingQueue = null;

        this.shuffled = false;
    }

    public void addPlayer(Player player) {
        validatePlayerForAddition(player);
        activePlayers.add(player);
    }

    // Invariante repetida: um jogador não entra duas vezes na sessão
    private void validatePlayerForAddition(Player player) {
        // Retirar (player.getId == null) (Construtor SEMPRE inicializa este atributo)
        if(player == null || player.getId() == null)
            throw new ValidationException("Player or player ID cannot be null");

        boolean alreadyExists = activePlayers.stream()
                .anyMatch(p-> p.getId().equals(player.getId()));

        if (alreadyExists) {
            throw new ConflictException("Player already in session");
        }
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null)
            throw new ValidationException("Player Id cannot be null");

        boolean removed = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!removed){
            throw new NotFoundException("Player not found in session");
        }
    }

    public Team findPlayerTeam(UUID playerId) {
        if(playerId == null) {
            throw new ValidationException("Player ID cannot be null");
        }

        return teams.stream()
                .filter(team -> team.getPlayers().stream()
                        .anyMatch(player ->
                                player.getId().equals(playerId)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Player is not in a team"));
    }

    // Nome de atributo melhor? (trazer + clareza)
    public void setTeams(List<Team> teams) {
        if(teams == null){
            throw new ValidationException("Teams cannot be null");
        }

        this.teams = teams;
    }

    public void removeTeam(Team team) {
        if(team == null) {
            throw new ValidationException("Team can't be null");
        }

        // Validação duplicada? (Se time estiver na partida atual, então a sessão já iniciou)
        if(hasStarted() && isCurrentMatchTeam(team))
            throw new ConflictException(
                    "Team cannot be removed if the session has not started or is currently in a match"
            );

        teams.remove(team);

        if(waitingQueue != null)
            waitingQueue.remove(team);
    }

    // hasStarted() deveria validar se a sessão iniciou
    // Se a sessão iniciou temos: WaitingQueue (mesmo que vazia) e CurrentMatch
    // Nome de metodo melhor? (trazer + clareza)
    public boolean hasStarted() {
        return currentMatch != null && waitingQueue != null;
    }

    public boolean isCurrentMatchTeam(Team team) {
        if(!hasStarted())
            return false;

        return currentMatch.getTeamA().equals(team) || currentMatch.getTeamB().equals(team);
    }

    public void setCurrentMatch(Match match) {
        if(match == null) {
            throw new ValidationException("Match cannot be null");
        }

        this.currentMatch = match;
    }

    // Nome de metodo melhor? (trazer + clareza)
    public void startQueue() {
        if (hasStarted()) {
            throw new ConflictException("Session already started with current match and waiting queue");
        }

        if(teams.size() < INITIAL_TEAMS){
            throw new ConflictException("Not enough teams to start");
        }

        this.waitingQueue = new WaitingQueue(teams);

        Team t1 = waitingQueue.poll();
        Team t2 = waitingQueue.poll();

        this.currentMatch = new Match(t1, t2);
    }

    public List<Team> getQueue() {
        return waitingQueue == null
                ? List.of()
                : waitingQueue.asList();
    }

    public void addTeamToQueue(Team team) {
        if(waitingQueue == null)
            throw new ConflictException("Waiting queue not initialized");

        waitingQueue.add(team);
    }

    public Team removeFirstTeamFromQueue() {
        if(waitingQueue == null)
            throw new ConflictException("Waiting queue not initialized");

        return waitingQueue.poll();
    }

    public boolean hasWaitingQueue() {
        return waitingQueue != null;
    }

    public boolean hasEnoughPlayersForDraw() {
        if(waitingQueue == null)
            return false;

        return waitingQueue.hasEnoughForDraw();
    }

    public int getWaitingPlayersCount() {
        if(waitingQueue == null)
            return 0;

        return waitingQueue.playersCount();
    }

    public void markAsShuffled() {
        this.shuffled = true;
    }
}
