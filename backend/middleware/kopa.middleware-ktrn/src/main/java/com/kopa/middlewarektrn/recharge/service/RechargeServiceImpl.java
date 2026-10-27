package com.kopa.middlewarektrn.recharge.service;

import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.KtrnApiClient;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.recharge.dto.RechargeCancelRequest;
import com.kopa.middlewarektrn.recharge.dto.RechargeRequest;
import com.kopa.middlewarektrn.recharge.dto.RechargeResponse;
import com.kopa.middlewarektrn.recharge.dto.RechargeVoucherRequest;
import org.springframework.stereotype.Service;

@Service
public class RechargeServiceImpl implements RechargeService {

    private static final String SVCCODE = "HP00002";
    private static final String OPCODE_RECHARGE = "HPOP001";
    private static final String OPCODE_CANCEL = "HPOP002";
    private static final String OPCODE_VOUCHER = "HPOP003";

    /** Distinguished Request Type - "01 : Pivot Access" per the doc, the only value it documents. */
    private static final String REQ_SYS_ID_PIVOT = "01";
    private static final String RCHRG_TP_DATA = "100";
    private static final String RCHRG_TP_VOUCHER = "300";
    private static final String RCHRG_STAT_RECHARGE = "10";
    private static final String RCHRG_STAT_CANCEL = "30";

    private final KtrnApiClient apiClient;
    private final KtrnProperties properties;

    public RechargeServiceImpl(KtrnApiClient apiClient, KtrnProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public RechargeResponse recharge(RechargeRequest request) {
        String ispCd = resolveIspCd(request.ispCd());

        String apiRequest = wrap(
                "<MSISDN>" + request.msisdn() + "</MSISDN>"
                        + "<NATIONAL_ID>" + request.nationalId() + "</NATIONAL_ID>"
                        + "<ISP_CD>" + ispCd + "</ISP_CD>"
                        + "<REQ_SYS_ID>" + REQ_SYS_ID_PIVOT + "</REQ_SYS_ID>"
                        + "<RCHRG_TP>" + RCHRG_TP_DATA + "</RCHRG_TP>"
                        + "<RCHRG_STAT>" + RCHRG_STAT_RECHARGE + "</RCHRG_STAT>"
                        + "<RCHRG_APPR_NO>" + request.approvalNo() + "</RCHRG_APPR_NO>"
                        + "<RCHRG_AMT>" + request.amount() + "</RCHRG_AMT>",
                OPCODE_RECHARGE);

        return toResponse(apiClient.callGeneric(apiRequest));
    }

    @Override
    public RechargeResponse cancel(RechargeCancelRequest request) {
        String ispCd = resolveIspCd(request.ispCd());

        String apiRequest = wrap(
                "<MSISDN>" + request.msisdn() + "</MSISDN>"
                        + "<NATIONAL_ID>" + request.nationalId() + "</NATIONAL_ID>"
                        + "<ISP_CD>" + ispCd + "</ISP_CD>"
                        + "<REQ_SYS_ID>" + REQ_SYS_ID_PIVOT + "</REQ_SYS_ID>"
                        + "<RCHRG_TP>" + RCHRG_TP_DATA + "</RCHRG_TP>"
                        + "<RCHRG_STAT>" + RCHRG_STAT_CANCEL + "</RCHRG_STAT>"
                        + "<RCHRG_APPR_NO>" + request.approvalNo() + "</RCHRG_APPR_NO>"
                        + "<CNL_AMT>" + request.cancelAmount() + "</CNL_AMT>",
                OPCODE_CANCEL);

        return toResponse(apiClient.callGeneric(apiRequest));
    }

    @Override
    public RechargeResponse rechargeByVoucher(RechargeVoucherRequest request) {
        String ispCd = resolveIspCd(request.ispCd());

        String apiRequest = wrap(
                "<MSISDN>" + request.msisdn() + "</MSISDN>"
                        + "<ISP_CD>" + ispCd + "</ISP_CD>"
                        + "<RCHRG_TP>" + RCHRG_TP_VOUCHER + "</RCHRG_TP>"
                        + "<RCHRG_STAT>" + RCHRG_STAT_RECHARGE + "</RCHRG_STAT>"
                        + "<VO_SEQ_NO>" + request.voucherNumber() + "</VO_SEQ_NO>",
                OPCODE_VOUCHER);

        return toResponse(apiClient.callGeneric(apiRequest));
    }

    private String wrap(String bodyXml, String opcode) {
        return "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>"
                + "<request>"
                + "<header>"
                + "<svcode>" + SVCCODE + "</svcode>"
                + "<opcode>" + opcode + "</opcode>"
                + "<CHCODE>A</CHCODE>"
                + "</header>"
                + "<body>" + bodyXml + "</body>"
                + "</request>";
    }

    private static RechargeResponse toResponse(KtrnGenericXmlResponse response) {
        KtrnGenericXmlResponse.Header header = response.getHeader();
        return new RechargeResponse(
                header != null ? header.getMsgcode() : null,
                header != null ? header.getMessage() : null
        );
    }

    private String resolveIspCd(String requestIspCd) {
        return (requestIspCd == null || requestIspCd.isBlank()) ? properties.getIspCd() : requestIspCd;
    }
}
