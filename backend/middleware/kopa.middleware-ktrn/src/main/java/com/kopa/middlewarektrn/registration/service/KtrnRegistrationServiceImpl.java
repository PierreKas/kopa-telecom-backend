package com.kopa.middlewarektrn.registration.service;

import com.kopa.middlewarektrn.common.crypto.KtrnAes256Util;
import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.registration.dto.SearchCustomerRequest;
import com.kopa.middlewarektrn.registration.dto.SearchCustomerResponse;
import com.kopa.middlewarektrn.registration.dto.SearchMsisdnByIccidRequest;
import com.kopa.middlewarektrn.registration.dto.SearchMsisdnByIccidResponse;
import com.kopa.middlewarektrn.registration.dto.SubscribeCustomerRequest;
import com.kopa.middlewarektrn.registration.dto.SubscribeCustomerResponse;
import org.springframework.stereotype.Service;

@Service
public class KtrnRegistrationServiceImpl implements KtrnRegistrationService {

    private static final String SVCCODE_HP00005 = "HP00005";
    private static final String SVCCODE_HP00001 = "HP00001";
    private static final String OPCODE_SUBSCRIBE = "HPOP501";
    private static final String OPCODE_SEARCH_CUSTOMER = "HPOP502";
    private static final String OPCODE_SEARCH_MSISDN = "HPOP003";

    private final KtrnApiClient apiClient;
    private final KtrnProperties properties;

    public KtrnRegistrationServiceImpl(KtrnApiClient apiClient, KtrnProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public SubscribeCustomerResponse subscribeCustomer(SubscribeCustomerRequest request) {
        String encryptedCorpRegNo = KtrnAes256Util.encrypt(request.corpRegNo());

        StringBuilder body = new StringBuilder();
        appendIfPresent(body, "SO_ID", request.soId());
        appendIfPresent(body, "LOGIN_ID", request.loginId());
        body.append("<ORG_ID>").append(request.orgId()).append("</ORG_ID>");
        body.append("<CUST_TP>").append(request.custTp()).append("</CUST_TP>");
        body.append("<CUST_NM>").append(request.custNm()).append("</CUST_NM>");
        appendIfPresent(body, "CORP_REG_NO", encryptedCorpRegNo);
        appendIfPresent(body, "BIZ_REG_NO", request.bizRegNo());
        body.append("<BIRTH_DAY>").append(request.birthDay()).append("</BIRTH_DAY>");
        appendIfPresent(body, "EMAIL", request.email());
        appendIfPresent(body, "PHONE_NUM", request.phoneNum());
        body.append("<ADDR_DTL>").append(request.addrDtl()).append("</ADDR_DTL>");

        String apiRequest = wrap(SVCCODE_HP00005, OPCODE_SUBSCRIBE, body.toString());
        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        return new SubscribeCustomerResponse(
                response.bodyValue("CUST_ID"),
                response.bodyValue("CUST_NM"),
                response.bodyValue("BIRTH_DAY"),
                response.bodyValue("ADDR_DTL"),
                response.bodyValue("EMAIL"),
                response.bodyValue("PHONE_NUM")
        );
    }

    @Override
    public SearchCustomerResponse searchCustomer(SearchCustomerRequest request) {
        String encryptedCorpRegNo = KtrnAes256Util.encrypt(request.corpRegNo());

        StringBuilder body = new StringBuilder();
        body.append("<SO_ID>").append(request.soId()).append("</SO_ID>");
        body.append("<CUST_TP>").append(request.custTp()).append("</CUST_TP>");
        body.append("<CUST_NM>").append(request.custNm()).append("</CUST_NM>");
        appendIfPresent(body, "CORP_REG_NO", encryptedCorpRegNo);

        String apiRequest = wrap(SVCCODE_HP00005, OPCODE_SEARCH_CUSTOMER, body.toString());
        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        return new SearchCustomerResponse(
                response.bodyValue("CUST_ID"),
                response.bodyValue("CUST_NM")
        );
    }

    @Override
    public SearchMsisdnByIccidResponse searchMsisdnByIccid(SearchMsisdnByIccidRequest request) {
        String ispCd = blankToNull(request.ispCd()) != null ? request.ispCd() : properties.getIspCd();

        StringBuilder body = new StringBuilder();
        body.append("<ICCID>").append(request.iccid()).append("</ICCID>");
        appendIfPresent(body, "NATIONAL_ID", request.nationalId());
        appendIfPresent(body, "ISP_CD", ispCd);

        String apiRequest = wrap(SVCCODE_HP00001, OPCODE_SEARCH_MSISDN, body.toString());
        KtrnGenericXmlResponse response = apiClient.callGeneric(apiRequest);

        return new SearchMsisdnByIccidResponse(
                response.bodyValue("SO_ID"),
                response.bodyValue("SO_NM"),
                response.bodyValue("MSISDN")
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

    private static void appendIfPresent(StringBuilder body, String tag, String value) {
        if (value != null && !value.isBlank()) {
            body.append('<').append(tag).append('>').append(value).append("</").append(tag).append('>');
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
