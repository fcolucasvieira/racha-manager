package com.fcolucasvieira.racha_manager.dto;

import jakarta.validation.constraints.NotNull;

public record FinishMatchRequestDTO(
        @NotNull
        int winnerTeamNumber) {
}
