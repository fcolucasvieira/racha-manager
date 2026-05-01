package com.fcolucasvieira.racha_manager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Match {
    private final Team teamA;
    private final Team teamB;

    public Team getLoser(Team winner) {
        if (winner.equals(teamA)) return teamB;
        if (winner.equals(teamB)) return teamA;

        throw new IllegalArgumentException("Winner is not part of this match");
    }
}
