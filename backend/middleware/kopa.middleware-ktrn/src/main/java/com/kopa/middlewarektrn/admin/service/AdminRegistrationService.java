package com.kopa.middlewarektrn.admin.service;

import com.kopa.middlewarektrn.admin.dto.AdminActivateResponse;
import com.kopa.middlewarektrn.admin.dto.AdminActivateSimRequest;
import com.kopa.middlewarektrn.admin.dto.AdminCreateCustomerRequest;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Admin-portal-driven customer create+activate, verified against the confirmed curl run in
 * ktrn_create_and_activate_customer.md and <b>split into two independent steps (2026-11-03)</b>
 * so a slow/timed-out activation can never undo, retry, or block on a customer creation that
 * already succeeded - a customer can exist here with no SIM activated yet, and
 * {@link #activateSim} can be called later, any number of times, once a usable {@code custId}
 * is known (from {@link #createCustomer}'s response).
 *
 * <p>There is no working HPOP201 login/HPOP313-password-encrypt step for this flow: KTRN never
 * issues a session, and the two KTRN-side identities below are literal constants, exactly as
 * KTRN's own confirmed run uses them. SO_ID/ORG_ID are retailer constants (KOPA Telecom);
 * CREATE_LOGIN_ID is the numeric staff code HPOP501 expects; ACTIVATE_LOGIN_ID (TRUPOC) is what
 * HPOP302 expects - confirmed these are genuinely different values, not a typo. PROD_CD is the
 * fixed literal "KOPA TelecomMainProduct". BIZ_REG_NO "4" is the literal value from the one
 * confirmed-working call; its real meaning for customer types other than individual (CUST_TP A)
 * is unconfirmed.
 */
@Service
public class AdminRegistrationService {

    private static final String SVCCODE = "HP00005";
    private static final String SO_ID = "20";
    private static final String ORG_ID = "3472";
    private static final String CREATE_LOGIN_ID = "5807";
    private static final String ACTIVATE_LOGIN_ID = "TRUPOC";
    private static final String PROD_CD = "KOPA TelecomMainProduct";
    private static final String BIZ_REG_NO = "4";
    private static final String MSGCODE_SUCCESS = "1001";
    private static final String MSGCODE_ALREADY_EXISTS = "-4003";
    private static final DateTimeFormatter BIRTH_DAY_IN = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter BIRTH_DAY_OUT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final KtrnApiClient apiClient;

    public AdminRegistrationService(KtrnApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /** HPOP601 free-check -> HPOP313 encrypt the national ID -> HPOP501 create (tolerating
     *  "-4003 already exists", CUST_ID comes back directly in the response, HPOP502 only as a
     *  fallback). Does not touch the SIM/ICCID at all - see {@link #activateSim}. */
    public AdminActivateResponse createCustomer(AdminCreateCustomerRequest request) {
        KtrnGenericXmlResponse free = call("HPOP601",
                "<MSISDN>" + request.msisdn() + "</MSISDN><SO_ID>" + SO_ID + "</SO_ID>");
        if (!"Y".equalsIgnoreCase(free.bodyValue("RESULT"))) {
            return new AdminActivateResponse(false, null, free.getHeader().getMsgcode(),
                    "This MSISDN is not available.");
        }

        String corpRegNo = encrypt(request.nationalId());
        String birthDay = LocalDate.parse(request.birthDay(), BIRTH_DAY_IN).format(BIRTH_DAY_OUT);

        KtrnGenericXmlResponse create = call("HPOP501",
                "<SO_ID>" + SO_ID + "</SO_ID>"
                        + "<LOGIN_ID>" + CREATE_LOGIN_ID + "</LOGIN_ID>"
                        + "<ORG_ID>" + ORG_ID + "</ORG_ID>"
                        + "<CUST_TP>" + request.custType() + "</CUST_TP>"
                        + "<CUST_NM>" + request.fullName() + "</CUST_NM>"
                        + "<CORP_REG_NO>" + corpRegNo + "</CORP_REG_NO>"
                        + "<BIZ_REG_NO>" + BIZ_REG_NO + "</BIZ_REG_NO>"
                        + "<BIRTH_DAY>" + birthDay + "</BIRTH_DAY>"
                        + "<EMAIL>" + (request.email() != null ? request.email() : "") + "</EMAIL>"
                        + "<PHONE_NUM>" + request.msisdn() + "</PHONE_NUM>"
                        + "<ADDR_DTL>" + request.addressDetail() + "</ADDR_DTL>"
                        + geoFields(request.longitude(), request.latitude()));
        String createCode = create.getHeader() != null ? create.getHeader().getMsgcode() : null;
        if (!MSGCODE_SUCCESS.equals(createCode) && !MSGCODE_ALREADY_EXISTS.equals(createCode)) {
            return new AdminActivateResponse(false, null, createCode,
                    create.getHeader() != null ? create.getHeader().getMessage() : "Customer creation failed.");
        }

        String custId = create.bodyValue("CUST_ID");
        if (custId == null || custId.isBlank()) {
            KtrnGenericXmlResponse lookup = call("HPOP502",
                    "<SO_ID>" + SO_ID + "</SO_ID>"
                            + "<CUST_TP>" + request.custType() + "</CUST_TP>"
                            + "<CUST_NM>" + request.fullName() + "</CUST_NM>"
                            + "<CORP_REG_NO>" + corpRegNo + "</CORP_REG_NO>");
            custId = lookup.bodyValue("CUST_ID");
            if (custId == null || custId.isBlank()) {
                return new AdminActivateResponse(false, null, lookup.getHeader().getMsgcode(),
                        "Could not retrieve the KTRN customer ID.");
            }
        }

        return new AdminActivateResponse(true, custId, MSGCODE_SUCCESS, "Customer created.");
    }

    /** HPOP601 free-check (re-checked here too, in case the number was taken between the two
     *  steps) -> HPOP302 activate, using the custId the create step already returned. */
    public AdminActivateResponse activateSim(AdminActivateSimRequest request) {
        KtrnGenericXmlResponse free = call("HPOP601",
                "<MSISDN>" + request.msisdn() + "</MSISDN><SO_ID>" + SO_ID + "</SO_ID>");
        if (!"Y".equalsIgnoreCase(free.bodyValue("RESULT"))) {
            return new AdminActivateResponse(false, request.custId(), free.getHeader().getMsgcode(),
                    "This MSISDN is not available.");
        }

        KtrnGenericXmlResponse activate = call("HPOP302",
                "<SO_ID>" + SO_ID + "</SO_ID>"
                        + "<MSISDN>" + request.msisdn() + "</MSISDN>"
                        + "<PROD_CD>" + PROD_CD + "</PROD_CD>"
                        + "<LOGIN_ID>" + ACTIVATE_LOGIN_ID + "</LOGIN_ID>"
                        + "<ORG_ID>" + ORG_ID + "</ORG_ID>"
                        + "<ICCID>" + request.iccid() + "</ICCID>"
                        + "<CUST_ID>" + request.custId() + "</CUST_ID>"
                        + "<REQ_SYS_ID>01</REQ_SYS_ID>");
        String activateCode = activate.getHeader() != null ? activate.getHeader().getMsgcode() : null;
        if (!MSGCODE_SUCCESS.equals(activateCode)) {
            return new AdminActivateResponse(false, request.custId(), activateCode,
                    activate.getHeader() != null ? activate.getHeader().getMessage() : "Activation failed.");
        }

        return new AdminActivateResponse(true, request.custId(), MSGCODE_SUCCESS, "Activated.");
    }

    private String geoFields(String longitude, String latitude) {
        if (longitude == null || latitude == null) {
            return "";
        }
        return "<LONGITUDE>" + longitude + "</LONGITUDE><LATITUDE>" + latitude + "</LATITUDE>";
    }

    /** HPOP313 intermittently returns "-4002" for some values (documented precedent); safe to
     *  retry once here since encrypt is a pure lookup with no side effects. */
    private String encrypt(String plaintext) {
        String body = "<PASSWORD>" + plaintext + "</PASSWORD><OPERATION_TYPE></OPERATION_TYPE>";
        KtrnGenericXmlResponse response = call("HPOP313", body);
        if (!MSGCODE_SUCCESS.equals(response.getHeader() != null ? response.getHeader().getMsgcode() : null)) {
            response = call("HPOP313", body);
        }
        return response.bodyValue("RESULT");
    }

    private KtrnGenericXmlResponse call(String opcode, String bodyXml) {
        String apiRequest = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request><header><svcode>" + SVCCODE + "</svcode><opcode>" + opcode
                + "</opcode><CHCODE>A</CHCODE></header><body>" + bodyXml + "</body></request>";
        return apiClient.callGeneric(apiRequest);
    }
}
