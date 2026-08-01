package com.fcolucasvieira.racha_manager.player.dto.response;

import java.util.UUID;

public record PlayerDTO(
        UUID id,
        String name
) {}
