package com.fcolucasvieira.racha_manager.dto;

import java.util.UUID;

public record PlayerDTO(
        UUID id,
        String name
) {
}
