package com.fcolucasvieira.racha_manager.infrastructure.controller;

import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerRequest;
import com.fcolucasvieira.racha_manager.application.dto.CreatePlayerResponse;
import com.fcolucasvieira.racha_manager.application.usecase.CreatePlayerUseCase;
import com.fcolucasvieira.racha_manager.infrastructure.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(
        name = "Players",
        description = "Operations related to player management"
)
@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {
    private final CreatePlayerUseCase createUseCase;


    @Operation(
            summary = "Create a new player",
            description = "Creates a new player and returns its unique identifier."
    )
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
