package com.fcolucasvieira.racha_manager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Session {

    private final UUID id;
    private final List<PlayerEntity> activePlayers;
    private List<Team> teams;
    private boolean shuffled;

    public Session() {
        this.id = UUID.randomUUID();
        this.activePlayers = new ArrayList<>();
        this.teams = new ArrayList<>();
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

        boolean playerRemoved = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!playerRemoved){
            throw new IllegalArgumentException("Player not found in session");
        }
    }

    public void updateTeams(List<Team> teams) {
        if(teams == null){
            throw new IllegalArgumentException("Teams cannot be null");
        }

        this.teams = teams;

        this.activePlayers.clear();
        this.activePlayers.addAll(
                teams.stream()
                        .flatMap(team -> team.getPlayers().stream())
                        .toList()
        );
    }

    private void validatePlayerForAddition(PlayerEntity player) {
        if(player == null || player.getId() == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        boolean containsPlayer = activePlayers.stream()
                .anyMatch(p -> p.getId().equals(player.getId()));

        if (containsPlayer) {
            throw new IllegalArgumentException("Player already in session");
        }
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

    public void markAsShuffled() {
        this.shuffled = true;
    }
}
