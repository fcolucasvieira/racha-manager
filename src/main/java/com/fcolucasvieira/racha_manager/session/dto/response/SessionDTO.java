package com.fcolucasvieira.racha_manager.session.dto.response;

import java.util.List;
import java.util.UUID;

// Verificar se posso reaproveitar esse DTO apenas para exibir o ID
public record SessionDTO(
        UUID id,
        boolean started,
        MatchDTO currentMatch,
        List<TeamDTO> queue
) {}
