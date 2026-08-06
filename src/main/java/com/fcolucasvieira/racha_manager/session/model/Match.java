package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import lombok.Getter;

// (Futuro) Match deverá conhecer resultado da partida
@Getter
public class Match {
    private final Team teamA;
    private final Team teamB;

    public Match(Team teamA, Team teamB) {
        if(teamA == null || teamB == null)
            throw new ValidationException("Teams can't be null");

        if(teamA.equals(teamB))
            throw new ConflictException("A match must have two different teams");

        this.teamA = teamA;
        this.teamB = teamB;
    }

    public Team getLoser(Team winner) {
        if(winner == null)
            throw new ValidationException("Winner can't be null");

        if (winner.equals(teamA)) return teamB;
        if (winner.equals(teamB)) return teamA;

        throw new ValidationException("Winner is not part of this match");
    }

    public boolean contains(Team team) {
        return teamA.equals(team) || teamB.equals(team);
    }
}
