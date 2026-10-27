package com.kopa.middlewarektrn.balance.service;

import com.kopa.middlewarektrn.balance.dto.BalanceLookupRequest;
import com.kopa.middlewarektrn.balance.dto.BalanceLookupResponse;
import com.kopa.middlewarektrn.balance.dto.BalanceSummaryRequest;
import com.kopa.middlewarektrn.balance.dto.BalanceSummaryResponse;
import com.kopa.middlewarektrn.balance.dto.BundleHistoryEntry;
import com.kopa.middlewarektrn.common.KtrnListFieldParser;
import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.gateway.dto.KtrnXmlResponse;
import org.springframework.stereotype.Service;

@Service
public class KtrnBalanceServiceImpl implements KtrnBalanceService {

    private static final String SVCCODE = "HP00001";
    private static final String OPCODE = "HPOP001";

    private static final String SVCCODE_SUMMARY = "HP00005";
    private static final String OPCODE_SUMMARY = "HPOP101";

    private final KtrnApiClient apiClient;
    private final KtrnProperties properties;

    public KtrnBalanceServiceImpl(KtrnApiClient apiClient, KtrnProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public BalanceLookupResponse checkBalance(BalanceLookupRequest request) {
        String ispCd = blankToNull(request.ispCd()) != null ? request.ispCd() : properties.getIspCd();
        String chcode = blankToNull(request.chcode()) != null ? request.chcode() : properties.getDefaultChcode();

        // Built exactly per RBSS-CCBS API_ACCESS s2.1.1's worked Request XML example, including its
        // "<svcode>" spelling (the schema table above it says "svccode", but the sample payload -
        // presumably captured from the real gateway - spells it without the second "c").
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE + "</opcode>"
                + "<CHCODE>" + chcode + "</CHCODE>"
                + "</header>"
                + "<body>"
                + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                + "<ISP_CD>" + ispCd + "</ISP_CD>"
                + "</body>"
                + "</request>";

        KtrnXmlResponse response = apiClient.call(apiRequest);
        KtrnXmlResponse.Header header = response.getHeader();
        KtrnXmlResponse.Body body = response.getBody();

        return new BalanceLookupResponse(
                header != null ? header.getSvccode() : SVCCODE,
                header != null ? header.getOpcode() : OPCODE,
                header != null ? header.getChcode() : chcode,
                header != null ? header.getMsgcode() : null,
                header != null ? header.getMessage() : null,
                body != null ? body.getRemainAmt() : null
        );
    }

    @Override
    public BalanceSummaryResponse checkSummary(BalanceSummaryRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE_SUMMARY + "</svcode>"
                + "<opcode>" + OPCODE_SUMMARY + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);
        KtrnGenericXmlResponse.Header header = response.getHeader();

        var bundleHistory = KtrnListFieldParser.parseRecords(response.bodyValue("BUNDLE_HIST"), ";", fields ->
                new BundleHistoryEntry(
                        field(fields, 0), field(fields, 1), field(fields, 2),
                        field(fields, 3), field(fields, 4), field(fields, 5)
                ));

        return new BalanceSummaryResponse(
                header != null ? header.getMsgcode() : null,
                header != null ? header.getMessage() : null,
                response.bodyValue("MSISDN"),
                response.bodyValue("SO_ID"),
                response.bodyValue("SO_NM"),
                response.bodyValue("REMAIN_AMT"),
                response.bodyValue("TOTAL_VOLUME"),
                response.bodyValue("AMOUNT_USED"),
                response.bodyValue("TODAY_VOLUME"),
                response.bodyValue("REMAIN_VOLUME"),
                response.bodyValue("PROD_NM"),
                response.bodyValue("PROD_GRP"),
                response.bodyValue("APP_USE_YN"),
                bundleHistory
        );
    }

    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index] : null;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
