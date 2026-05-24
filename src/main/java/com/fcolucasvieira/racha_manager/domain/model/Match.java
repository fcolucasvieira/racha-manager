package com.fcolucasvieira.racha_manager.domain.model;

import lombok.Getter;

@Getter
public class Match {
    private final Team teamA;
    private final Team teamB;

    public Match(Team teamA, Team teamB) {
        if(teamA == null || teamB == null) {
            throw new IllegalArgumentException("Teams cannot be null");
        }

        if(teamA.equals(teamB)) {
            throw new IllegalArgumentException("A match must have two different teams");
        }

        this.teamA = teamA;
        this.teamB = teamB;
    }

    public Team getLoser(Team winner) {
        if (winner.equals(teamA)) return teamB;
        if (winner.equals(teamB)) return teamA;

        throw new IllegalArgumentException("Winner is not part of this match");
    }

    public boolean contains(Team team) {
        return teamA.equals(team) || teamB.equals(team);
    }
}
