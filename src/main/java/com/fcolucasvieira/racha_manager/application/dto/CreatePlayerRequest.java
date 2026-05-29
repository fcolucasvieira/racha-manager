package com.fcolucasvieira.racha_manager.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(
        @NotBlank(message = "Player name cannot be blank")
        String name
) {
}
