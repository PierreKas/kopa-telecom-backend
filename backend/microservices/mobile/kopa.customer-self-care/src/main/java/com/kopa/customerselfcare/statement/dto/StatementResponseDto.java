package com.kopa.customerselfcare.statement.dto;

import java.util.List;

public record StatementResponseDto(String msisdn, List<StatementEntryDto> entries) {
}
