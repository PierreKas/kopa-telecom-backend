package com.kopa.middlewarektrn.bundle.dto;

import java.util.List;

public record BundleListResponse(
        List<BundleItem> items
) {
}
