package com.kopa.middlewarektrn.bundle.service;

import com.kopa.middlewarektrn.bundle.dto.BundleListRequest;
import com.kopa.middlewarektrn.bundle.dto.BundleListResponse;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseCheckRequest;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseCheckResponse;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseRequest;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseResponse;
import com.kopa.middlewarektrn.bundle.dto.EWalletCheckRequest;
import com.kopa.middlewarektrn.bundle.dto.EWalletCheckResponse;

public interface BundleService {

    /** RBSS-CCBS API_ACCESS s2.1.3 (HP00001/HPOP005). */
    BundleListResponse listAvailableBundles(BundleListRequest request);

    /** RBSS-CCBS API_ACCESS s2.3.1 (HP00002/HPOP004). */
    BundlePurchaseResponse purchase(BundlePurchaseRequest request);

    /** RBSS-CCBS API_ACCESS s2.4.1 (HP00005/HPOP403). */
    BundlePurchaseCheckResponse checkPurchase(BundlePurchaseCheckRequest request);

    /** RBSS-CCBS API_ACCESS s2.4.2 (HP00005/HPOP402). */
    EWalletCheckResponse checkEWallet(EWalletCheckRequest request);
}
