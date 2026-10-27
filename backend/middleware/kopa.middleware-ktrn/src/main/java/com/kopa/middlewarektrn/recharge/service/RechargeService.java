package com.kopa.middlewarektrn.recharge.service;

import com.kopa.middlewarektrn.recharge.dto.RechargeCancelRequest;
import com.kopa.middlewarektrn.recharge.dto.RechargeRequest;
import com.kopa.middlewarektrn.recharge.dto.RechargeResponse;
import com.kopa.middlewarektrn.recharge.dto.RechargeVoucherRequest;

public interface RechargeService {

    /** RBSS-CCBS API_ACCESS s2.2.1 (HP00002/HPOP001). */
    RechargeResponse recharge(RechargeRequest request);

    /** RBSS-CCBS API_ACCESS s2.2.2 (HP00002/HPOP002). */
    RechargeResponse cancel(RechargeCancelRequest request);

    /** RBSS-CCBS API_ACCESS s2.2.3 (HP00002/HPOP003). */
    RechargeResponse rechargeByVoucher(RechargeVoucherRequest request);
}
