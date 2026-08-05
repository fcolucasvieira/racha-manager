package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.session.constant.RachaRules;

import java.util.LinkedList;
import java.util.List;

public class WaitingQueue {
    // Uso de LinkedList (complexidade O(1)) em inserções/remoções
    // em ínicio, meio e fim da fila
    private final LinkedList<Team> teams;

    public WaitingQueue(List<Team> teams) {
        if (teams == null) {
            throw new ValidationException("Teams can't be null");
        }

        this.teams = new LinkedList<>(teams);
    }

    public void add(Team team) {
        if(team == null)
            throw new ValidationException("Team cannot be null");

        if(teams.contains(team))
            throw new ConflictException("Team already exists in queue");

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

    public Team poll() {
        if(teams.isEmpty()) {
            throw new ValidationException("Teams in queue can't be empty");
        }

        return teams.removeFirst();
    }

    // isEmpty(), contains(), size() são metodos derivados nascem de atributos
    // (desnecessário implementar)

    public int playersCount() {
        return teams.stream()
                .mapToInt(t -> t.getPlayers().size())
                .sum();
    }

    public boolean hasEnoughForDraw() {
        return playersCount() >= RachaRules.INITIAL_PLAYERS;
    }

    public List<Team> asList() {
        return List.copyOf(teams);
    }
}
