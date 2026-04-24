package com.fcolucasvieira.racha_manager.dto;

import java.util.UUID;

public record SessionActivePlayerResponse(UUID id,
                                          String name) {
}
