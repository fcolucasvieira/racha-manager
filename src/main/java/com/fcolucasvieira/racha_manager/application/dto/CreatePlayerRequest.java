package com.fcolucasvieira.racha_manager.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(
        @NotBlank
        String name
) {
}
