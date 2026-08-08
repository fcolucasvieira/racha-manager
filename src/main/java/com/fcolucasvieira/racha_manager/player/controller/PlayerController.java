package com.fcolucasvieira.racha_manager.player.controller;

import com.fcolucasvieira.racha_manager.player.dto.request.CreatePlayerRequest;
import com.fcolucasvieira.racha_manager.player.dto.response.CreatePlayerResponse;
import com.fcolucasvieira.racha_manager.player.dto.response.PlayerDTO;
import com.fcolucasvieira.racha_manager.player.usecase.CreatePlayerUseCase;
import com.fcolucasvieira.racha_manager.common.response.ApiResponse;
import com.fcolucasvieira.racha_manager.player.usecase.GetAllPlayersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final GetAllPlayersUseCase getAllUseCase;

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

    @Operation(
            summary = "Get all players",
            description = """
    Retrieves a paginated list of players.
    
    Example request: 
    
    GET /players?page=0&size=20

    Returns:
    - Player unique identifier (id)
    - Player name
    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlayerDTO>>> getAll(
            @PageableDefault(page = 0, size = 16)
            @ParameterObject Pageable pageable) {
        var response = getAllUseCase.execute(pageable)
                .map(player ->
                        new PlayerDTO(player.getId(), player.getName()));

        return ResponseEntity.ok(
                ApiResponse.success(response, "All players retrieved successfully")
        );
    }
}
