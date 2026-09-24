package com.fcolucasvieira.racha_manager.player.model;

import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.user.model.Provider;
import com.fcolucasvieira.racha_manager.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    void shouldCreatePlayerSuccessfully() {
        String namePlayer = "João";

        User userPlayer = new User(
                Provider.LOCAL,
                (UUID.randomUUID()).toString(),
                "lucas@gmail.com",
                "Lucas Vieira"
        );

        Player player = new Player(namePlayer, userPlayer);

        assertNotNull(player);
        assertEquals(namePlayer, player.getName());
        assertEquals(userPlayer, player.getUser());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void shouldThrowValidationExceptionWhenNameIsInvalid(String invalidName) {
        User userPlayer = new User(
                Provider.LOCAL,
                (UUID.randomUUID()).toString(),
                "lucas@gmail.com",
                "Lucas Vieira"
        );

        assertThrows(ValidationException.class, () -> new Player(invalidName, userPlayer));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserIsNull() {
        String name = "Lucas";

        assertThrows(NotFoundException.class, () -> new Player(name, null));
    }

}