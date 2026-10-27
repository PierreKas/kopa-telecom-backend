package com.kopa.middlewarektrn.usage.service;

import com.kopa.middlewarektrn.common.KtrnListFieldParser;
import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.usage.dto.UsageItem;
import com.kopa.middlewarektrn.usage.dto.UsageSearchRequest;
import com.kopa.middlewarektrn.usage.dto.UsageSearchResponse;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {

    private static final String SVCCODE = "HP00001";
    private static final String OPCODE = "HPOP008";

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

        var items = KtrnListFieldParser.parseRecords(response.bodyValue("PURCHASED_LIST"), fields ->
                new UsageItem(
                        field(fields, 0), field(fields, 1), field(fields, 2),
                        field(fields, 3), field(fields, 4), field(fields, 5)
                ));

        return new UsageSearchResponse(items);
    }

    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index] : null;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
