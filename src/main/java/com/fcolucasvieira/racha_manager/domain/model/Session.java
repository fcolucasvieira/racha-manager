package com.fcolucasvieira.racha_manager.domain.model;

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
        this.queue = new ArrayList<>();
        this.shuffled = false;
    }

    public void addPlayer(PlayerEntity player) {
        validatePlayerForAddition(player);
        activePlayers.add(player);
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }

        boolean removed = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!removed){
            throw new IllegalArgumentException("Player not found in session");
        }
    }

    private void validatePlayerForAddition(PlayerEntity player) {
        if(player == null || player.getId() == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        boolean alreadyExists = activePlayers.stream()
                .anyMatch(p-> p.getId().equals(player.getId()));

        if (alreadyExists) {
            throw new IllegalArgumentException("Player already in session");
        }
    }

    public void updateTeams(List<Team> teams) {
        if(teams == null){
            throw new IllegalArgumentException("Teams cannot be null");
        }

        this.teams = teams;
    }

    public void reorderPlayers(List<PlayerEntity> newOrder) {
        if(newOrder == null) {
            throw new IllegalArgumentException("Player list can't be null");
        }

        if(newOrder.size() != this.activePlayers.size()) {
            throw new IllegalArgumentException("Invalid reorder size");
        }

        this.activePlayers.clear();
        this.activePlayers.addAll(newOrder);
    }

    // regras de negócio (fila) em session
    public void startQueue() {
        if (currentMatch != null) {
            throw new IllegalStateException("Queue already started");
        }

        if(teams.size() < 2){
            throw new IllegalStateException("Not enough teams to start");
        }

        this.queue = new ArrayList<>(teams);

        // armazena os dois primeiros times da fila (jogo atual)
        Team t1 = queue.removeFirst();
        Team t2 = queue.removeFirst();

        this.currentMatch = new Match(t1, t2);
    }

    // regras de negócio (fila) em session
    public void finishMatch(int winnerTeamNumber) {
        if (currentMatch == null) {
            throw new IllegalStateException("No match in progress");
        }

        Team winner = currentMatch.getTeamA().getNumber() == winnerTeamNumber
                ? currentMatch.getTeamA()
                : currentMatch.getTeamB();

        Team loser = currentMatch.getLoser(winner);

        // loser vai pro final da fila
        queue.add(loser);

        // se não tiver próximo, encerra
        if (queue.isEmpty()) {
            currentMatch = null;
            return;
        }

        // armazena o primeiro time da fila (time atual a jogar)
        Team next = queue.removeFirst();

        currentMatch = new Match(winner, next);
    }

    public void markAsShuffled() {
        this.shuffled = true;
    }
}
