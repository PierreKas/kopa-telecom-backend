package com.kopa.middlewarektrn.msisdn.service;

import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnStatusRequest;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnStatusResponse;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnSuggestionsRequest;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnSuggestionsResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MsisdnServiceImpl implements MsisdnService {

    private static final String SVCCODE = "HP00005";
    private static final String OPCODE_STATUS = "HPOP601";
    private static final String OPCODE_SUGGESTIONS = "HPOP602";

    private final KtrnApiClient apiClient;

    public MsisdnServiceImpl(KtrnApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public MsisdnStatusResponse checkStatus(MsisdnStatusRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_STATUS + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                + "<SO_ID>" + request.soId() + "</SO_ID>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);
        return new MsisdnStatusResponse("Y".equalsIgnoreCase(response.bodyValue("RESULT")));
    }

    @Override
    public MsisdnSuggestionsResponse recommend(MsisdnSuggestionsRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_SUGGESTIONS + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<SO_ID>" + request.soId() + "</SO_ID>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);
        return new MsisdnSuggestionsResponse(splitCsv(response.bodyValue("MSISDN")));
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
