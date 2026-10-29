package com.kopa.middlewarektrn.bundle.dto;

import java.util.List;

/** msgcode/message are passed through so the caller can tell a genuine "no bundles available"
 *  apart from a failed KTRN call - same passthrough convention as BalanceLookupResponse. */
public record BundleListResponse(
        String msgcode,
        String message,
        List<BundleItem> items
) {
}
