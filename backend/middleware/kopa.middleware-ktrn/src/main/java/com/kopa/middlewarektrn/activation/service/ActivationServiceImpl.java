package com.kopa.middlewarektrn.activation.service;

import com.kopa.middlewarektrn.activation.dto.PricePlanRequest;
import com.kopa.middlewarektrn.activation.dto.PricePlanResponse;
import com.kopa.middlewarektrn.activation.dto.SimpleActivationRequest;
import com.kopa.middlewarektrn.activation.dto.SimpleActivationResponse;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ActivationServiceImpl implements ActivationService {

    private static final String SVCCODE = "HP00005";
    private static final String OPCODE_PRICE_PLAN = "HPOP301";
    private static final String OPCODE_SIMPLE_ACTIVATE = "HPOP302";

    /**
     * Distinguished Request Type. The doc's own table (s2.5.2) labels "02" as "App Access", which
     * reads as the obvious choice for a mobile app - but live-tested 2026-10-27 against the real
     * gateway, "02" fails HPOP302 with "-6002 Undefined value/incorrect value PROD_CD,SO_ID was
     * entered." (a misleading message - PROD_CD/SO_ID were valid) while "01" succeeds (MSGCODE
     * 1001) with the exact same PROD_CD/SO_ID. KTRN's own test traffic also uses "01". Confirmed,
     * not the doc's labeled value.
     */
    private static final String REQ_SYS_ID_APP_ACCESS = "01";

    private final KtrnApiClient apiClient;

    public ActivationServiceImpl(KtrnApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PricePlanResponse searchPricePlans(PricePlanRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_PRICE_PLAN + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                + "<ICCID>" + request.iccid() + "</ICCID>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        int productCount = parseIntOrZero(response.bodyValue("PROD_CNT"));
        List<String> productCodes = splitCsv(response.bodyValue("PROD_CD"));
        List<String> productNames = splitCsv(response.bodyValue("PROD_NM"));
        List<String> productGroups = splitCsv(response.bodyValue("PROD_GRP"));

        return new PricePlanResponse(
                response.bodyValue("SO_ID"),
                response.bodyValue("MSISDN"),
                productCount,
                productCodes,
                productNames,
                productGroups
        );
    }

    @Override
    public SimpleActivationResponse simpleActivate(SimpleActivationRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_SIMPLE_ACTIVATE + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<SO_ID>" + request.soId() + "</SO_ID>"
                + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                + "<PROD_CD>" + request.prodCd() + "</PROD_CD>"
                + "<LOGIN_ID>" + request.loginId() + "</LOGIN_ID>"
                + "<ORG_ID>" + request.orgId() + "</ORG_ID>"
                + "<ICCID>" + request.iccid() + "</ICCID>"
                + "<CUST_ID>" + request.custId() + "</CUST_ID>"
                + "<REQ_SYS_ID>" + REQ_SYS_ID_APP_ACCESS + "</REQ_SYS_ID>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        return new SimpleActivationResponse(
                response.bodyValue("RESULT"),
                response.bodyValue("PROD_RATE")
        );
    }

    private static int parseIntOrZero(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
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
