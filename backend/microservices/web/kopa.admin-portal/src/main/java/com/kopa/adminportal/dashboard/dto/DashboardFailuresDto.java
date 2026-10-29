package com.kopa.adminportal.dashboard.dto;

import java.util.List;

public record DashboardFailuresDto(
        long totalFailed,
        List<FailureBreakdownEntryDto> byLayer
) {
}
