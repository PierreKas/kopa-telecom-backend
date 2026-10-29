package com.kopa.middlewarektrn.usage.dto;

import java.util.List;

/** msgcode/message are passed through so the caller can tell a genuine "no active bundles" apart
 *  from a failed KTRN call - same passthrough convention as BalanceLookupResponse. */
public record UsageSearchResponse(
        String msgcode,
        String message,
        List<UsageItem> items
) {
}
