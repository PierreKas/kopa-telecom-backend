package com.kopa.adminportal.dashboard.dto;

public record FailureBreakdownEntryDto(
        String failureLayer,
        long count
) {
}
