package com.kopa.adminportal.mirror.repository;

import com.kopa.adminportal.mirror.model.Transaction;
import com.kopa.adminportal.mirror.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByMsisdn(String msisdn);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByMsisdnAndStatus(String msisdn, TransactionStatus status);

    long countByRequestedAtAfter(Instant since);

    long countByStatusAndRequestedAtAfter(TransactionStatus status, Instant since);

    @Query("select t.failureLayer as failureLayer, count(t) as total from Transaction t "
            + "where t.status = 'FAILED' group by t.failureLayer")
    List<FailureCount> countFailuresByLayer();

    interface FailureCount {
        String getFailureLayer();
        long getTotal();
    }
}
