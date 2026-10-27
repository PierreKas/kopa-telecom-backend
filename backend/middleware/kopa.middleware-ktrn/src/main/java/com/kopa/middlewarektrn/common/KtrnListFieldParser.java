package com.kopa.middlewarektrn.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * KTRN packs some response fields as a composite list: records separated by "|", fields within
 * each record separated by ",". Used by PURCHASED_LIST (usage, s2.1.4) and PROD_LIST (bundles,
 * s2.1.3). Blank/missing input yields an empty list rather than a list with one blank record.
 */
public final class KtrnListFieldParser {

    private KtrnListFieldParser() {
    }

    public static <T> List<T> parseRecords(String rawValue, Function<String[], T> recordMapper) {
        return parseRecords(rawValue, "\\|", recordMapper);
    }

    /** Same as {@link #parseRecords(String, Function)} but with an explicit record separator -
     *  BUNDLE_HIST (balance summary, s2.1.5) uses ";" instead of the usual "|". */
    public static <T> List<T> parseRecords(String rawValue, String recordSeparator, Function<String[], T> recordMapper) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        for (String record : rawValue.split(recordSeparator)) {
            if (record.isBlank()) {
                continue;
            }
            String[] fields = Arrays.stream(record.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
            result.add(recordMapper.apply(fields));
        }
        return result;
    }
}
