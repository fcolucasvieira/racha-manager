package com.fcolucasvieira.racha_manager.dto;

import com.fcolucasvieira.racha_manager.domain.enums.MatchResultType;
import jakarta.validation.constraints.NotNull;

public record FinishMatchRequest(
        Integer winnerTeamNumber,

        @NotNull
        MatchResultType resultType
) {
}
