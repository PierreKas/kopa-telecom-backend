package com.kopa.middlewarektrn.usage.dto;

import java.util.List;

public record UsageSearchResponse(
        List<UsageItem> items
) {
}
