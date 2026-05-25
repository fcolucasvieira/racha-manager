package com.fcolucasvieira.racha_manager.application.dto;

import java.util.List;

public record TeamDTO(
        int number,
        List<PlayerDTO> players
) {
}
