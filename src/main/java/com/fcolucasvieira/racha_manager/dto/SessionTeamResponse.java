package com.fcolucasvieira.racha_manager.dto;

import java.util.List;

public record SessionTeamResponse(
        int number,
        List<String> playerNames
) {
}
