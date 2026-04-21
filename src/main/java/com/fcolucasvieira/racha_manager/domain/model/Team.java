package com.fcolucasvieira.racha_manager.domain.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Team {
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

    public void validatePlayerForAddition(PlayerEntity player) {
        if(player == null || player.getId() == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        boolean containsPlayer = players.stream()
                .anyMatch(p -> p.getId().equals(player.getId()));

        if (containsPlayer) {
            throw new IllegalArgumentException("Player already in team");
        }
    }
}
