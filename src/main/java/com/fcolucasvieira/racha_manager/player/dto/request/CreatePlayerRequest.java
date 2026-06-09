package com.fcolucasvieira.racha_manager.player.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(
        @Schema(
                example = "Lucas Vieira",
                description = "Player name"
        )
        @NotBlank(message = "Player name cannot be blank")
        String name
) {
}
