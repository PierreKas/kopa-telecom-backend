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
import java.time.Instant;

/** Configurable, not hardcoded - the Scope of Work states transaction limits are a Kopa-provided
 *  business rule, so this table is the mechanism; seeded values are placeholders (see Flyway
 *  migration) until Kopa confirms the real thresholds. */
@Entity
@Table(name = "transaction_limit_policy")
@Getter
@Setter
public class TransactionLimitPolicy {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, unique = true)
    private TransactionType transactionType;

    @Column(name = "max_amount_per_tx")
    private BigDecimal maxAmountPerTx;

    @Column(name = "max_amount_per_day")
    private BigDecimal maxAmountPerDay;

    @Column(name = "max_count_per_day")
    private Integer maxCountPerDay;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
