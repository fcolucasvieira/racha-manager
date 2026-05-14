package com.fcolucasvieira.racha_manager.dto;

import java.util.List;

public record TeamDTO(
        int number,
        List<PlayerDTO> players
) {
}
