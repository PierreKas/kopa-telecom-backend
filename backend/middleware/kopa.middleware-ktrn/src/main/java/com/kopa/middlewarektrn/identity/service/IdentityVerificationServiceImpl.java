package com.kopa.middlewarektrn.identity.service;

import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.identity.dto.NidaVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.NidaVerificationResponse;
import com.kopa.middlewarektrn.identity.dto.PassportVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.PassportVerificationResponse;
import com.kopa.middlewarektrn.identity.dto.RraVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.RraVerificationResponse;
import org.springframework.stereotype.Service;

/** All three operations here hit KtrnApiClient.callGenericExternal(...) - the
 *  .../voucher/api/external_access.jsp host, not the api_access.jsp one everything else in this
 *  service uses (RBSS-CCBS API_ACCESS s2.5.3-2.5.5). */
@Service
public class IdentityVerificationServiceImpl implements IdentityVerificationService {

    private static final String SVCCODE = "HP00005";
    private static final String OPCODE_NIDA = "HPOP303";
    private static final String OPCODE_RRA = "HPOP305";
    private static final String OPCODE_DGIE = "HPOP306";

    private final KtrnApiClient apiClient;

    public IdentityVerificationServiceImpl(KtrnApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public NidaVerificationResponse verifyNida(NidaVerificationRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_NIDA + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<NID>" + request.nationalId() + "</NID>"
                + "<PHOTO>" + request.photoBase64() + "</PHOTO>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGenericExternal(apiRequest);

        return new NidaVerificationResponse(
                response.bodyValue("RESULT"),
                response.bodyValue("MSG"),
                response.bodyValue("PHOTO"),
                response.bodyValue("SIGNATURE"),
                response.bodyValue("NID"),
                response.bodyValue("NAME"),
                response.bodyValue("BIRTHDAY"),
                response.bodyValue("GENDER")
        );
    }

    @Override
    public RraVerificationResponse verifyRra(RraVerificationRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_RRA + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<TIN>" + request.tin() + "</TIN>"
                + "<REGR_ID>" + request.registerId() + "</REGR_ID>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGenericExternal(apiRequest);

        return new RraVerificationResponse(
                response.bodyValue("MESSAGECODE"),
                response.bodyValue("MESSAGEDESCRIPTION"),
                response.bodyValue("SUPPLIERNAME"),
                response.bodyValue("SUPPLIERTIN"),
                response.bodyValue("EMAILADDRESS"),
                response.bodyValue("PHONENUMBER"),
                response.bodyValue("PROVINCE"),
                response.bodyValue("DISTRICT"),
                response.bodyValue("SECTOR"),
                response.bodyValue("CELL"),
                response.bodyValue("CATEGORY")
        );
    }

    @Override
    public PassportVerificationResponse verifyPassport(PassportVerificationRequest request) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + OPCODE_DGIE + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>"
                + "<DOCNUMBER>" + request.docNumber() + "</DOCNUMBER>"
                + "<ISSUEDBY>" + request.issuedBy() + "</ISSUEDBY>"
                + "<REGR_ID>" + request.registerId() + "</REGR_ID>"
                + "</body>"
                + "</request>";

        KtrnGenericXmlResponse response = apiClient.callGenericExternal(apiRequest);

        return new PassportVerificationResponse(
                response.bodyValue("STATUS"),
                response.bodyValue("MESSAGE"),
                response.bodyValue("PERSONID"),
                response.bodyValue("FIRSTNAME"),
                response.bodyValue("LASTNAME"),
                response.bodyValue("DATEOFBIRTH"),
                response.bodyValue("DOCNUMBER"),
                response.bodyValue("ISSUEDBY"),
                response.bodyValue("NATIONALITY"),
                response.bodyValue("EXPIRYDATE"),
                response.bodyValue("SEX")
        );
    }
}
