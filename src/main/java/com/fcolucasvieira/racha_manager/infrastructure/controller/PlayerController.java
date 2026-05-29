package com.fcolucasvieira.racha_manager.infrastructure.controller;

import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerRequest;
import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerResponse;
import com.fcolucasvieira.racha_manager.application.usecase.CreatePlayerUseCase;
import com.fcolucasvieira.racha_manager.infrastructure.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<CreatePlayerResponse>> create(@RequestBody @Valid CreatePlayerRequest request) {
        UUID id = createUseCase.execute(request.name());

        CreatePlayerResponse response = new CreatePlayerResponse(id);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(response, "Player created successfully")
                );
    }
}
