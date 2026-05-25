package com.fcolucasvieira.racha_manager.infraestructure.controller;

import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerRequest;
import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerResponse;
import com.fcolucasvieira.racha_manager.application.usecase.CreatePlayerUseCase;
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
    public ResponseEntity<CreatePlayerResponse> create(@RequestBody @Valid CreatePlayerRequest request) {
        UUID id = createUseCase.execute(request.name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatePlayerResponse(id));
    }
}
