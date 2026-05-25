package com.fcolucasvieira.racha_manager.application.dto;

import java.util.UUID;

public record PlayerDTO(
        UUID id,
        String name
) {
}
