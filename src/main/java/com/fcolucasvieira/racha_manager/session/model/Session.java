package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.session.constant.SessionRules;
import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.PlayerEntity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Session {

    private final UUID id;
    private final List<PlayerEntity> activePlayers;
    private List<Team> teams;
    private List<Team> queue;
    private Match currentMatch;
    private boolean shuffled;

    public Session() {
        this.id = UUID.randomUUID();
        this.activePlayers = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.queue = null;
        this.shuffled = false;
    }

    // Player

    public void addPlayer(PlayerEntity player) {
        validatePlayerForAddition(player);
        activePlayers.add(player);
    }

    private void validatePlayerForAddition(PlayerEntity player) {
        if(player == null || player.getId() == null) {
            throw new ValidationException("Player cannot be null");
        }

        boolean alreadyExists = activePlayers.stream()
                .anyMatch(p-> p.getId().equals(player.getId()));

        if (alreadyExists) {
            throw new ConflictException("Player already in session");
        }
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null) {
            throw new ValidationException("Player ID cannot be null");
        }

        boolean removed = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!removed){
            throw new NotFoundException("Player not found in session");
        }
    }

    // Busca time em que o jogador está
    public Team findPlayerTeam(UUID playerId) {
        return teams.stream()
                .filter(team -> team.getPlayers().stream()
                        .anyMatch(player ->
                                player.getId().equals(playerId)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Player is not in a team"));
    }

    // Team

    public void updateTeams(List<Team> teams) {
        if(teams == null){
            throw new ValidationException("Teams cannot be null");
        }

        this.teams = teams;
    }

    public void removeTeam(Team team) {
        if(team == null) {
            throw new ValidationException("Team cannot be null");
        }

        if(teams != null) {
            teams.remove(team);
        }

        if(queue != null){
            queue.remove(team);
        }
    }

    public void validateTeamRemoval(Team team) {
        if (!hasStarted()) {
            return;
        }

        if (isCurrentMatchTeam(team)) {
            throw new ConflictException("Cannot remove all players from a current match team");
        }
    }

    // CurrentMatch

    public boolean hasStarted() {
        return currentMatch != null;
    }

    public boolean isCurrentMatchTeam(Team team) {
        if(!hasStarted()) {
            return false;
        }

        return currentMatch.getTeamA().equals(team) || currentMatch.getTeamB().equals(team);
    }

    public void updateCurrentMatch(Match match) {
        if(match == null) {
            throw new ValidationException("Match cannot be null");
        }

        this.currentMatch = match;
    }

    // Queue

    public void startQueue() {
        if (currentMatch != null) {
            throw new ConflictException("Queue already started");
        }

        if(teams.size() < 2){
            throw new ConflictException("Not enough teams to start");
        }

        this.queue = new ArrayList<>(teams);

        // armazena os dois primeiros times da fila (jogo atual)
        Team t1 = queue.removeFirst();
        Team t2 = queue.removeFirst();

        this.currentMatch = new Match(t1, t2);
    }

    public List<Team> getQueue() {
        return queue == null ? List.of() : List.copyOf(queue);
    }

    public void addTeamToQueue(Team team) {
        if(team == null) {
            throw new ValidationException("Team cannot be null");
        }

        if(this.queue == null) {
            throw new ConflictException("Queue not initialized");
        }

        if(this.queue.contains(team)) {
            throw new ConflictException("Team already in queue");
        }

        // se o time ainda não jogou, adicioná-lo atrás do último novato da fila
        if(!team.isPlayed()) {

            // gera uma lista com times novatos através da queue
            List<Team> rookieTeams = queue.stream()
                    .filter(t -> !t.isPlayed())
                    .toList();

            // se a lista estiver vazia, não há times novatos. logo, o time novato é adicionado ao início da queue
            if(rookieTeams.isEmpty()) {
                this.queue.addFirst(team);

                return;
            }

            // seleta o último time da queue como novato
            Team lastRookie = rookieTeams.getLast();

            // seleta índice do último time novato na queue
            int rookieIndex = queue.indexOf(lastRookie);

            this.queue.add(rookieIndex + 1, team);

            return;
        }

        // caso contrário, adicioná-lo ao final da fila
        this.queue.add(team);
    }

    public Team removeFirstTeamFromQueue() {
        if(queue == null || queue.isEmpty()) {
            throw new ConflictException("Queue is empty");
        }

        return queue.removeFirst();
    }

    public boolean hasQueue() {
        return queue != null;
    }

    public void clearQueue() {
        if (this.queue != null) {
            this.queue.clear();
        }
        this.currentMatch = null;
    }

    public boolean hasEnoughPlayersForDraw() {
        return getWaitingPlayersCount() >= (SessionRules.TEAM_SIZE * 2);
    }

    public int getWaitingPlayersCount() {
        if(queue == null){
            return 0;
        }

        return queue.stream().
                mapToInt(team -> team.getPlayers().size())
                .sum();
    }

    public boolean canStartQueue() {
        return !hasStarted() && teams != null && teams.size() >= 2;
    }

    // Shuffled

    public void markAsShuffled() {
        this.shuffled = true;
    }
}
