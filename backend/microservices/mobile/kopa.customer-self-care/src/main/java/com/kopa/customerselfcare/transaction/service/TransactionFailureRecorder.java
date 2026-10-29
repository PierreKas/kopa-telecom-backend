package com.kopa.customerselfcare.transaction.service;

import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.model.TransactionStatus;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Persists a transaction's FAILED outcome in its OWN, independent database transaction
 * (REQUIRES_NEW). BundleServiceImpl.purchase()/RechargeServiceImpl.recharge()/cancel() are
 * themselves @Transactional, and the normal way they reject a request is to throw an exception -
 * which makes Spring roll back everything that happened inside that method call, including an
 * inline "mark as FAILED" save that would otherwise silently undo itself along with everything
 * else. Confirmed live 2026-10-28: a recharge that failed on a middleware timeout never actually
 * persisted as FAILED, which let its already-used OTP code be replayed successfully on retry -
 * the transaction had silently reverted to OTP_PENDING. Calling this from a separate Spring bean
 * (not a same-class private method) is required for REQUIRES_NEW to actually take effect, since
 * Spring's @Transactional proxying doesn't intercept self-invocation.
 */
@Service
public class TransactionFailureRecorder {

    private final TransactionRepository transactions;

    public TransactionFailureRecorder(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Transaction transaction, String failureLayer, String detail) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureLayer(failureLayer);
        transaction.setFailureDetail(detail);
        transaction.setCompletedAt(Instant.now());
        transaction.setDurationMs(transaction.getCompletedAt().toEpochMilli() - transaction.getRequestedAt().toEpochMilli());
        transactions.save(transaction);
    }
}
