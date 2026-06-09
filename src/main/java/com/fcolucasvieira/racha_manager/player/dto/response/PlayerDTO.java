package com.fcolucasvieira.racha_manager.player.dto.response;

import java.util.UUID;

// Verificar se é possível reaproveitar este DTO como resposta apenas com ID
public record PlayerDTO(
        UUID id,
        String name
) {}
