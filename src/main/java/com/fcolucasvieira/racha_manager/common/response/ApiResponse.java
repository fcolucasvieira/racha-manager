package com.fcolucasvieira.racha_manager.common.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
        ) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>(false, message, null, LocalDateTime.now());
    }
}
