package com.kopa.customerselfcare.limit.repository;

import com.kopa.customerselfcare.limit.model.TransactionLimitPolicy;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionLimitPolicyRepository extends JpaRepository<TransactionLimitPolicy, String> {

    Optional<TransactionLimitPolicy> findByTransactionTypeAndActiveTrue(TransactionType transactionType);
}
