package com.kopa.customeridentity.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDto(
        String code,
        String message,
        List<FieldErrorDto> details
) {
}
