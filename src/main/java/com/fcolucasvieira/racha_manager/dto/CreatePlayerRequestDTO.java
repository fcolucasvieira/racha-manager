package com.fcolucasvieira.racha_manager.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequestDTO(
        @NotBlank
        String name
) {
}
