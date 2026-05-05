package com.fcolucasvieira.racha_manager.domain.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Team {
    private static final int TEAM_SIZE = 4;

    private final int number;

    private final List<PlayerEntity> players;

    public Team(Integer number) {
        if(number <= 0) {
            throw new IllegalArgumentException("Team number must be valid");
        }

        this.number = number;
        this.players = new ArrayList<>();
    }

    public void addPlayer(PlayerEntity player) {
        validatePlayerForAddition(player);

        players.add(player);
    }

    public PlayerEntity removeFirstPlayer() {
        if (players.isEmpty()) {
            throw new IllegalStateException("Team has no players");
        }

        return players.remove(0);    }

    public void removePlayerById(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }

        boolean removed = players.removeIf(p -> p.getId().equals(playerId));

        if (!removed) {
            throw new IllegalArgumentException("Player not found in team");
        }
    }

    public void validatePlayerForAddition(PlayerEntity player) {
        if(player == null || player.getId() == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        boolean alreadyExists = players.stream()
                .anyMatch(p -> p.getId().equals(player.getId()));

        if (alreadyExists) {
            throw new IllegalArgumentException("Player already in team");
        }
    }

    public boolean isFull() {
        return players.size() == TEAM_SIZE;
    }

    public boolean isIncomplete() {
        return players.size() < TEAM_SIZE;
    }

    public int missingPlayers() {
        return Math.max(0, TEAM_SIZE - players.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team team)) return false;
        return number == team.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }
}
