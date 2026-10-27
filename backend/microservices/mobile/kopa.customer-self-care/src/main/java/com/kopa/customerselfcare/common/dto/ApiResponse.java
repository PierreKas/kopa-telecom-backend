package com.kopa.customerselfcare.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        ErrorDto error,
        String path,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, null, Instant.now());
    }

    public static ApiResponse<Void> error(String code, String message, String path) {
        return error(code, message, null, path);
    }

    public static ApiResponse<Void> error(String code, String message, List<FieldErrorDto> details, String path) {
        return new ApiResponse<>(false, null, null, new ErrorDto(code, message, details), path, Instant.now());
    }
}
