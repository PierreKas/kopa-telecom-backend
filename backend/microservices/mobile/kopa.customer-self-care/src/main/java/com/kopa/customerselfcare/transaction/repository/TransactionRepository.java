package com.kopa.customerselfcare.transaction.repository;

import com.kopa.customerselfcare.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByMsisdnOrderByRequestedAtDesc(String msisdn);
}
