package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.rules.RachaRules.TEAM_SIZE;

@Getter
public class Team {
    private final int number;
    private final List<Player> players;
    private boolean played;

    public Team(int number) {
        if(number <= 0)
            throw new ValidationException("Team number must be greater than 0");

        this.number = number;
        this.players = new ArrayList<>();
        this.played = false;
    }

    public void addPlayer(Player player) {
        if(player == null)
            throw new ValidationException("Player can't be null");

        boolean alreadyExists = players.stream()
                .anyMatch(p -> p.getId().equals(player.getId()));

        if (alreadyExists)
            throw new ConflictException("Player already in team");

        players.add(player);
    }

    public Player removeFirstPlayer() {
        if (players.isEmpty())
            throw new ConflictException("Team has no players");

        return players.removeFirst();
    }

    // Ajustar remoção por objeto
    public void removePlayerById(UUID playerId) {
        if (playerId == null)
            throw new ValidationException("Player I can't be null");

        boolean removed = players
                .removeIf(p -> p.getId().equals(playerId));

        if (!removed)
            throw new NotFoundException("Player not found in team with Id: " + playerId);
    }

    public boolean containsPlayer(UUID playerId) {
        if(playerId == null)
            throw new ValidationException("Player Id can't be null");

        return players.stream()
                .anyMatch(p -> p.getId().equals(playerId));
    }

    public void markAsPlayed() {
        this.played = true;
    }

    public boolean isFull() {
        return players.size() == TEAM_SIZE;
    }

    public boolean isIncomplete() {
        return players.size() < TEAM_SIZE;
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }

    public int playersCount() {
        return players.size();
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
