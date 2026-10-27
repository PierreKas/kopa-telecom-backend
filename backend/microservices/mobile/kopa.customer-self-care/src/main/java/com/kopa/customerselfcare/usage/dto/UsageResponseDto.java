package com.kopa.customerselfcare.usage.dto;

import java.util.List;

public record UsageResponseDto(String msisdn, List<UsageItemDto> items) {
}
