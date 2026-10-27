package com.kopa.customerselfcare.limit.service;

import com.kopa.customerselfcare.common.exception.TransactionLimitExceededException;
import com.kopa.customerselfcare.limit.model.DailyTransactionCounter;
import com.kopa.customerselfcare.limit.model.TransactionLimitPolicy;
import com.kopa.customerselfcare.limit.repository.DailyTransactionCounterRepository;
import com.kopa.customerselfcare.limit.repository.TransactionLimitPolicyRepository;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Enforced before a recharge/purchase ever reaches Middleware - see
 *  backend/architecture-reference.md s10. A transaction type with no active policy row is
 *  unlimited (no policy configured yet = nothing to enforce). */
@Service
public class TransactionLimitService {

    private final TransactionLimitPolicyRepository policies;
    private final DailyTransactionCounterRepository counters;

    public TransactionLimitService(TransactionLimitPolicyRepository policies,
                                   DailyTransactionCounterRepository counters) {
        this.policies = policies;
        this.counters = counters;
    }

    /** Checks the policy for this type and atomically increments today's counter if it passes.
     *  Throws {@link TransactionLimitExceededException} and leaves the counter untouched if not. */
    @Transactional
    public void checkAndRecord(String msisdn, TransactionType type, BigDecimal amount) {
        TransactionLimitPolicy policy = policies.findByTransactionTypeAndActiveTrue(type).orElse(null);
        if (policy == null) {
            return;
        }

        if (policy.getMaxAmountPerTx() != null && amount != null
                && amount.compareTo(policy.getMaxAmountPerTx()) > 0) {
            throw new TransactionLimitExceededException(
                    "Amount exceeds the maximum allowed per " + type + " transaction.");
        }

        LocalDate today = LocalDate.now();
        DailyTransactionCounter counter = counters.findForUpdate(msisdn, today, type)
                .orElseGet(() -> newCounter(msisdn, today, type));

        BigDecimal projectedAmount = counter.getTotalAmount().add(amount == null ? BigDecimal.ZERO : amount);
        int projectedCount = counter.getTotalCount() + 1;

        if (policy.getMaxAmountPerDay() != null && projectedAmount.compareTo(policy.getMaxAmountPerDay()) > 0) {
            throw new TransactionLimitExceededException(
                    "This would exceed the daily amount limit for " + type + ".");
        }
        if (policy.getMaxCountPerDay() != null && projectedCount > policy.getMaxCountPerDay()) {
            throw new TransactionLimitExceededException(
                    "This would exceed the daily transaction count limit for " + type + ".");
        }

        counter.setTotalAmount(projectedAmount);
        counter.setTotalCount(projectedCount);
        counters.save(counter);
    }

    private DailyTransactionCounter newCounter(String msisdn, LocalDate date, TransactionType type) {
        DailyTransactionCounter counter = new DailyTransactionCounter();
        counter.setId(UUID.randomUUID().toString());
        counter.setMsisdn(msisdn);
        counter.setTransactionDate(date);
        counter.setTransactionType(type);
        return counter;
    }
}
