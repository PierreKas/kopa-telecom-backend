package com.kopa.middlewarektrn.bundle.service;

import com.kopa.middlewarektrn.bundle.dto.BundleItem;
import com.kopa.middlewarektrn.bundle.dto.BundleListRequest;
import com.kopa.middlewarektrn.bundle.dto.BundleListResponse;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseCheckRequest;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseCheckResponse;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseRequest;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseResponse;
import com.kopa.middlewarektrn.bundle.dto.EWalletCheckRequest;
import com.kopa.middlewarektrn.bundle.dto.EWalletCheckResponse;
import com.kopa.middlewarektrn.common.KtrnListFieldParser;
import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import org.springframework.stereotype.Service;

@Service
public class BundleServiceImpl implements BundleService {

    private static final String SVCCODE_HP00002 = "HP00002";
    private static final String SVCCODE_HP00005 = "HP00005";
    // 2026-10-13: v2.0 doc's "Searching available bundles" section (2.1.1) is self-consistent -
    // schema table AND worked example both say HP00005/HPOP102. The old v1.9 code (HP00001/
    // HPOP005) used here previously got live "-6002 undefined/incorrect value MSISDN" even for a
    // KTRN-recognized MSISDN, consistent with a wrong opcode rather than a missing-subscriber
    // business error (which is what HP00001/HPOP001 balance returns instead, via MSGCODE -4003).
    private static final String OPCODE_LIST = "HPOP102";
    private static final String OPCODE_PURCHASE = "HPOP004";
    private static final String OPCODE_PURCHASE_CHECK = "HPOP403";
    private static final String OPCODE_EWALLET_CHECK = "HPOP402";

    private final KtrnApiClient apiClient;
    private final KtrnProperties properties;

    public BundleServiceImpl(KtrnApiClient apiClient, KtrnProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public BundleListResponse listAvailableBundles(BundleListRequest request) {
        String apiRequest = wrap(SVCCODE_HP00005, OPCODE_LIST,
                "<MSISDN>" + request.msisdn() + "</MSISDN>");

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        var items = KtrnListFieldParser.parseRecords(response.bodyValue("PROD_LIST"), fields ->
                new BundleItem(stripSoPrefix(field(fields, 0)), field(fields, 1), field(fields, 2)));

        return new BundleListResponse(
                response.getHeader() != null ? response.getHeader().getMsgcode() : null,
                response.getHeader() != null ? response.getHeader().getMessage() : null,
                items
        );
    }

    @Override
    public BundlePurchaseResponse purchase(BundlePurchaseRequest request) {
        String ispCd = blankToNull(request.ispCd()) != null ? request.ispCd() : properties.getIspCd();

        // The doc's own s2.3.1 schema table names this field ISP_CD, but its worked example uses
        // <SO_ID> with the exact same kind of value - same "trust the worked example" call as the
        // svcode/svccode spelling for feature 2.1.1. Using SO_ID here, matching the example.
        String apiRequest = wrap(SVCCODE_HP00002, OPCODE_PURCHASE,
                "<MSISDN>" + request.msisdn() + "</MSISDN>"
                        + "<SO_ID>" + ispCd + "</SO_ID>"
                        + "<RCHRG_TP>500</RCHRG_TP>"
                        + "<RCHRG_STAT>10</RCHRG_STAT>"
                        + "<PROD_CD>" + request.productCode() + "</PROD_CD>");

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);
        return new BundlePurchaseResponse(response.bodyValue("RESULT"));
    }

    @Override
    public BundlePurchaseCheckResponse checkPurchase(BundlePurchaseCheckRequest request) {
        String apiRequest = wrap(SVCCODE_HP00005, OPCODE_PURCHASE_CHECK,
                "<SO_ID>" + request.soId() + "</SO_ID>"
                        + "<PROD_CD>" + request.productCode() + "</PROD_CD>"
                        + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                        + "<PAYMENT_TYPE>01</PAYMENT_TYPE>"
                        + "<REQ_SYS_ID>01</REQ_SYS_ID>");

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);
        return new BundlePurchaseCheckResponse(
                response.bodyValue("RESULT"),
                response.getHeader() != null ? response.getHeader().getMsgcode() : null,
                response.getHeader() != null ? response.getHeader().getMessage() : null
        );
    }

    @Override
    public EWalletCheckResponse checkEWallet(EWalletCheckRequest request) {
        String apiRequest = wrap(SVCCODE_HP00005, OPCODE_EWALLET_CHECK,
                "<SO_ID>" + request.soId() + "</SO_ID>"
                        + "<PROD_CD>" + request.productCode() + "</PROD_CD>");

        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);
        return new EWalletCheckResponse(
                "Y".equalsIgnoreCase(response.bodyValue("RESULT")),
                response.getHeader() != null ? response.getHeader().getMsgcode() : null,
                response.getHeader() != null ? response.getHeader().getMessage() : null
        );
    }

    private static String wrap(String svccode, String opcode, String bodyXml) {
        return "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + svccode + "</svcode>"
                + "<opcode>" + opcode + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>" + bodyXml + "</body>"
                + "</request>";
    }

    private static String field(String[] fields, int index) {
        return index < fields.length ? fields[index] : null;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /**
     * 2026-10-29 live-confirmed: this KTRN environment's HPOP102 PROD_LIST entries come back
     * prefixed with a leading "<number>-" (e.g. "572-PD00067100") that neither the RBSS-CCBS doc's
     * worked example for this same field ("PD00045904", bare) nor PROD_CD's documented STRING(10)
     * format account for - the prefixed value is 14 chars. Passing it through unchanged to
     * HPOP403 (purchase-check) got back MSGCODE -7002 "Data is not found.." even for a MSISDN/
     * product pair that HPOP102 itself had just listed as available; HPOP402 (ewallet-check, which
     * never takes a MSISDN) returned RESULT Y for the same prefixed code regardless, so it isn't a
     * useful signal here. Stripping back to the bare PD-code is what the subscriber-specific
     * purchase-check/purchase calls actually expect.
     */
    private static String stripSoPrefix(String productCode) {
        if (productCode == null) {
            return null;
        }
        int dash = productCode.indexOf('-');
        return dash >= 0 ? productCode.substring(dash + 1) : productCode;
    }
}
