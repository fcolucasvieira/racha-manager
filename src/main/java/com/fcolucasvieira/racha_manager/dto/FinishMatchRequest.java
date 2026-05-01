package com.fcolucasvieira.racha_manager.dto;

import jakarta.validation.constraints.NotNull;

public record FinishMatchRequest(
        @NotNull
        int winnerTeamNumber) {
}
