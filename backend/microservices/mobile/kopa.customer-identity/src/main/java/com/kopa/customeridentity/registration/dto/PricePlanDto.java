package com.kopa.customeridentity.registration.dto;

import java.util.List;

/** Client-facing price plan list - zips the middleware's parallel arrays into one list of plans,
 *  since a mobile app renders a list of options, not three parallel arrays. */
public record PricePlanDto(
        String productCode,
        String productName,
        String productGroup
) {
    public static List<PricePlanDto> zip(List<String> codes, List<String> names, List<String> groups) {
        int size = Math.min(codes.size(), Math.min(names.size(), groups.size()));
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> new PricePlanDto(codes.get(i), names.get(i), groups.get(i)))
                .toList();
    }
}
