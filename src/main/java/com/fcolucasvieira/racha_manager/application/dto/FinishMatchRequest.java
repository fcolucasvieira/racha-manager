package com.fcolucasvieira.racha_manager.application.dto;

import com.fcolucasvieira.racha_manager.domain.enums.MatchResultType;
import jakarta.validation.constraints.NotNull;

public record FinishMatchRequest(
        Integer winnerTeamNumber,

        @NotNull(message = "Result type is required")
        MatchResultType resultType
) {
}
