package com.fcolucasvieira.racha_manager.controller;

import com.fcolucasvieira.racha_manager.dto.CreatePlayerRequestDTO;
import com.fcolucasvieira.racha_manager.dto.CreatePlayerResponseDTO;
import com.fcolucasvieira.racha_manager.usecase.CreatePlayerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {
    private final CreatePlayerUseCase createUseCase;

    @PostMapping
    public ResponseEntity<CreatePlayerResponseDTO> create(@RequestBody @Valid CreatePlayerRequestDTO request) {
        UUID id = createUseCase.execute(request.name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatePlayerResponseDTO(id));
    }
}
