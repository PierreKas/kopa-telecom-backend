package com.kopa.middlewarektrn.usage.service;

import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.usage.dto.UsageBalance;
import com.kopa.middlewarektrn.usage.dto.UsageItem;
import com.kopa.middlewarektrn.usage.dto.UsageSearchRequest;
import com.kopa.middlewarektrn.usage.dto.UsageSearchResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UsageServiceImpl implements UsageService {

    private static final String SVCCODE = "HP00001";
    private static final String OPCODE = "HPOP008";

    /** Matches each brace-wrapped ALL_BAL_LIST entry inside a PURCHASED_LIST record - there can
     *  be more than one (e.g. a combo bundle with both a DATA and a VOICE balance). */
    private static final Pattern BALANCE_PATTERN = Pattern.compile("\\{([^}]*)}");

    private final KtrnApiClient apiClient;
    private final KtrnProperties properties;

    public UsageServiceImpl(KtrnApiClient apiClient, KtrnProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public UsageSearchResponse searchUsage(UsageSearchRequest request) {
        String ispCd = blankToNull(request.ispCd()) != null ? request.ispCd() : properties.getIspCd();

        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                + "<ISP_CD>" + ispCd + "</ISP_CD>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        return new UsageSearchResponse(
                response.getHeader() != null ? response.getHeader().getMsgcode() : null,
                response.getHeader() != null ? response.getHeader().getMessage() : null,
                parseUsageItems(response.bodyValue("PURCHASED_LIST"))
        );
    }

    private static List<UsageItem> parseUsageItems(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        List<UsageItem> result = new ArrayList<>();
        for (String record : rawValue.split("\\|")) {
            if (!record.isBlank()) {
                result.add(parseUsageItem(record));
            }
        }
        return result;
    }

    private static UsageItem parseUsageItem(String record) {
        int firstBrace = record.indexOf('{');
        String head = firstBrace >= 0 ? record.substring(0, firstBrace) : record;
        if (head.endsWith(",")) {
            head = head.substring(0, head.length() - 1);
        }
        String[] fields = Arrays.stream(head.split(",")).map(String::trim).toArray(String[]::new);

        List<UsageBalance> balances = new ArrayList<>();
        Matcher matcher = BALANCE_PATTERN.matcher(record);
        while (matcher.find()) {
            // BAL_CATG,BAL_TYPE,ALLOWANCE,USAGE,REMAINING,BAL_EFF_DATETIME,BAL_EXP_DATETIME (s2.1.2)
            String[] b = Arrays.stream(matcher.group(1).split(",")).map(String::trim).toArray(String[]::new);
            balances.add(new UsageBalance(field(b, 0), field(b, 2), field(b, 3), field(b, 4)));
        }

        return new UsageItem(field(fields, 0), field(fields, 1), balances);
    }

    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index] : null;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
