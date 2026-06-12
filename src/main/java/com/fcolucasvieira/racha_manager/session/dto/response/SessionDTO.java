package com.fcolucasvieira.racha_manager.session.dto.response;

import com.fcolucasvieira.racha_manager.player.dto.response.PlayerDTO;

import java.util.List;
import java.util.UUID;

public record SessionDTO(
        UUID id,
        boolean started,
        List<PlayerDTO> activePlayers,
        MatchDTO currentMatch,
        List<TeamDTO> queue
) {}
