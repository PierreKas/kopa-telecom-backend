package com.kopa.customerselfcare.usage.client.dto;

import java.util.List;

public record UsageSearchResponse(String msgcode, String message, List<UsageItem> items) {
}
