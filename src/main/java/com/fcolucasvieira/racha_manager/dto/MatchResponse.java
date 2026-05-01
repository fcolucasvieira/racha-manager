package com.fcolucasvieira.racha_manager.dto;

public record MatchResponse(
        SessionTeamResponse teamA,
        SessionTeamResponse teamB
) {}
