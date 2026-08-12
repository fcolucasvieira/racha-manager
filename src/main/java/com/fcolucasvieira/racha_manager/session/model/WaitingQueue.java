package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.rules.RachaRules;

import java.util.LinkedList;
import java.util.List;

public class WaitingQueue {
    // Uso de LinkedList (complexidade O(1)) em inserções/remoções
    // em ínicio, meio e fim da fila
    private final LinkedList<Team> teams;

    public WaitingQueue(List<Team> teams) {
        if (teams == null)
            throw new ValidationException("Teams can't be null");

        this.teams = new LinkedList<>(teams);
    }

    public void add(Team team) {
        if(team == null)
            throw new ValidationException("Team can't be null");

        if(teams.contains(team))
            throw new ConflictException("Team already exists in waiting queue with Number: " + team.getNumber());

        if(!team.isPlayed()) {
            List<Team> rookies = teams.stream()
                    .filter(t -> !t.isPlayed())
                    .toList();

            if(rookies.isEmpty()) {
                teams.addFirst(team);
                return;
            }
                int indexLastRookie = teams.indexOf(
                        rookies.getLast()
                );

                teams.add(indexLastRookie + 1, team);
        } else {
            teams.addLast(team);
        }
    }

    public void remove(Team team) {
        if(team == null)
            throw new ValidationException("Team can't be null");

        if(!teams.remove(team))
            throw new NotFoundException("Team not found in waiting queue with Number: " + team.getNumber());
    }

    public Team poll() {
        if(teams.isEmpty())
            throw new ValidationException("Teams in waiting queue can't be empty");

        return teams.removeFirst();
    }

    public boolean isEmpty() {
        return teams.isEmpty();
    }

    public int playersCount() {
        return teams.stream()
                .mapToInt(Team::playersCount)
                .sum();
    }

    public boolean hasEnoughForDraw() {
        return playersCount() >= RachaRules.INITIAL_PLAYERS;
    }

    public List<Team> teams() {
        return List.copyOf(teams);
    }
}
