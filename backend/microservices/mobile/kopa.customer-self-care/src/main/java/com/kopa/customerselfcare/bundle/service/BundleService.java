package com.kopa.customerselfcare.bundle.service;

import com.kopa.customerselfcare.bundle.dto.BundleListResponseDto;
import com.kopa.customerselfcare.bundle.dto.PurchaseBundleRequest;
import com.kopa.customerselfcare.bundle.dto.PurchaseBundleResponse;
import com.kopa.customerselfcare.transaction.model.Transaction;

public interface BundleService {

    /** FR-07 - bundles available for purchase (HP00001/HPOP005). */
    BundleListResponseDto listAvailable(String msisdn);

    /** FR-08/FR-04 - purchase a bundle: e-wallet check -> purchase check, limit-checked,
     *  OTP-gated. Returns immediately with status OTP_PENDING - the actual purchase call happens
     *  in {@link #completePurchase(Transaction)} once the OTP is verified. */
    PurchaseBundleResponse purchase(String msisdn, PurchaseBundleRequest request);

    /** Called by TransactionOtpController once the OTP for a BUNDLE_PURCHASE transaction is
     *  verified - makes the deferred KTRN purchase call and finalizes the transaction. */
    PurchaseBundleResponse completePurchase(Transaction transaction);
}
