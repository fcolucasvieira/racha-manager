package com.fcolucasvieira.racha_manager.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(
        @NotBlank
        String name
) {
}
