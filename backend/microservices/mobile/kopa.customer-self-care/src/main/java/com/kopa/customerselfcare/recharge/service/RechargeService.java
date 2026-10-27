package com.kopa.customerselfcare.recharge.service;

import com.kopa.customerselfcare.recharge.dto.RechargeCancelRequestDto;
import com.kopa.customerselfcare.recharge.dto.RechargeRequestDto;
import com.kopa.customerselfcare.recharge.dto.RechargeResponseDto;
import com.kopa.customerselfcare.recharge.dto.RechargeVoucherRequestDto;
import com.kopa.customerselfcare.transaction.model.Transaction;

public interface RechargeService {

    /** FR-09/FR-04 - recharge by amount (HP00002/HPOP001), limit-checked, OTP-gated. Returns
     *  immediately with status OTP_PENDING - the actual KTRN call happens in
     *  {@link #completeRecharge(Transaction)} once the OTP is verified. */
    RechargeResponseDto recharge(String msisdn, RechargeRequestDto request);

    /** Called by TransactionOtpController once the OTP for a RECHARGE transaction is verified -
     *  makes the deferred KTRN call and finalizes the transaction. */
    RechargeResponseDto completeRecharge(Transaction transaction);

    /** FR-09 - recharge by voucher (HP00002/HPOP003), limit-checked by count only (no amount is
     *  known until KTRN processes the voucher). Not OTP-gated - a voucher is itself a possession
     *  factor. */
    RechargeResponseDto rechargeByVoucher(String msisdn, RechargeVoucherRequestDto request);

    /** Cancel a previous recharge (HP00002/HPOP002) - not limit-checked, cancels reduce exposure
     *  rather than increase it. Not OTP-gated. */
    RechargeResponseDto cancel(String msisdn, String transactionId, RechargeCancelRequestDto request);
}
