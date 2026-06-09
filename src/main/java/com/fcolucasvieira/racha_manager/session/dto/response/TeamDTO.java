package com.fcolucasvieira.racha_manager.session.dto.response;

import com.fcolucasvieira.racha_manager.player.dto.response.PlayerDTO;

import java.util.List;

public record TeamDTO(
        int number,
        List<PlayerDTO> players
) {
}
