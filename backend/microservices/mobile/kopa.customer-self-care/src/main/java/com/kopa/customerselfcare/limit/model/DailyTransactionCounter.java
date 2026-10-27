package com.kopa.customerselfcare.limit.model;

import com.kopa.customerselfcare.transaction.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row per (msisdn, transaction_date, transaction_type) - unique constraint enforced in the
 *  Flyway migration. Incremented atomically as transactions post, checked against
 *  TransactionLimitPolicy before a new one is allowed through. */
@Entity
@Table(name = "daily_transaction_counter")
@Getter
@Setter
public class DailyTransactionCounter {

    @Id
    private String id;

    @Column(nullable = false, length = 15)
    private String msisdn;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_count", nullable = false)
    private int totalCount = 0;
}
