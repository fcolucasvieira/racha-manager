package com.fcolucasvieira.racha_manager.session.dto.request;

import com.fcolucasvieira.racha_manager.session.enums.MatchResultType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FinishMatchRequest(
        @Schema(
                example = "1",
                description = "Winner team number. Must be null for draws."
        )
        Integer winnerTeamNumber,

        @Schema(
                example = "WINNER",
                description = "Result type: WINNER or DRAW"
        )
        @NotNull(message = "Result type is required")
        MatchResultType resultType
) {
}
