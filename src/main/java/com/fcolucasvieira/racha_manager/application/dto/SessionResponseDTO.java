package com.fcolucasvieira.racha_manager.application.dto;

import java.util.List;
import java.util.UUID;

public record SessionResponseDTO(
        UUID id,
        boolean started,
        MatchDTO currentMatch,
        List<TeamDTO> queues
) {}
