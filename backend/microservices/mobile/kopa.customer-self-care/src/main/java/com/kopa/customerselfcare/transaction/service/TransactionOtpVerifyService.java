package com.kopa.customerselfcare.transaction.service;

import com.kopa.customerselfcare.bundle.dto.PurchaseBundleResponse;
import com.kopa.customerselfcare.bundle.service.BundleService;
import com.kopa.customerselfcare.common.exception.BadRequestException;
import com.kopa.customerselfcare.common.exception.NotFoundException;
import com.kopa.customerselfcare.recharge.dto.RechargeResponseDto;
import com.kopa.customerselfcare.recharge.service.RechargeService;
import com.kopa.customerselfcare.transaction.dto.TransactionOtpVerifyResponse;
import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Completes any OTP-gated transaction (POST /api/v1/transactions/{msisdn}/{id}/otp/verify) -
 *  verifies the code, then dispatches to whichever service owns the deferred KTRN call based on
 *  transaction_type. RechargeService/BundleService don't know about each other or about this
 *  class; this is the only place that routes between them. */
@Service
public class TransactionOtpVerifyService {

    private final TransactionRepository transactions;
    private final TransactionOtpService otpService;
    private final RechargeService rechargeService;
    private final BundleService bundleService;

    public TransactionOtpVerifyService(TransactionRepository transactions, TransactionOtpService otpService,
                                        RechargeService rechargeService, BundleService bundleService) {
        this.transactions = transactions;
        this.otpService = otpService;
        this.rechargeService = rechargeService;
        this.bundleService = bundleService;
    }

    @Transactional
    public TransactionOtpVerifyResponse verifyAndComplete(String msisdn, String transactionId, String code) {
        Transaction transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("No such transaction."));
        if (!transaction.getMsisdn().equals(msisdn)) {
            throw new NotFoundException("No such transaction.");
        }

        otpService.verify(transaction, code);

        if (transaction.getTransactionType() == TransactionType.RECHARGE) {
            RechargeResponseDto response = rechargeService.completeRecharge(transaction);
            return new TransactionOtpVerifyResponse(response.transactionId(), response.status(), response.ktrnMessage());
        }
        if (transaction.getTransactionType() == TransactionType.BUNDLE_PURCHASE) {
            PurchaseBundleResponse response = bundleService.completePurchase(transaction);
            return new TransactionOtpVerifyResponse(response.transactionId(), response.status(), response.result());
        }

        throw new BadRequestException("This transaction type does not support OTP verification.");
    }
}
