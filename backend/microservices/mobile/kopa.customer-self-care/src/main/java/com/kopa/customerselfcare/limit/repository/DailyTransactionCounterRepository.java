package com.kopa.customerselfcare.limit.repository;

import com.kopa.customerselfcare.limit.model.DailyTransactionCounter;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTransactionCounterRepository extends JpaRepository<DailyTransactionCounter, String> {

    /** Row-locked so two concurrent requests for the same (msisdn, date, type) can't both read
     *  the same pre-increment totals and both pass a limit check that only one of them should. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM DailyTransactionCounter c WHERE c.msisdn = :msisdn "
            + "AND c.transactionDate = :date AND c.transactionType = :type")
    Optional<DailyTransactionCounter> findForUpdate(@Param("msisdn") String msisdn,
                                                     @Param("date") LocalDate date,
                                                     @Param("type") TransactionType type);
}
