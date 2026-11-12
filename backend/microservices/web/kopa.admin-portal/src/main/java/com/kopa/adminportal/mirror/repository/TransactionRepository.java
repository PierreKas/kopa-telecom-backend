package com.kopa.adminportal.mirror.repository;

import com.kopa.adminportal.mirror.model.Transaction;
import com.kopa.adminportal.mirror.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // Newest-first - matches kopa.customer-self-care's own findByMsisdnOrderByRequestedAtDesc,
    // the source of truth this table mirrors, rather than leaving list order to Postgres's
    // unspecified (effectively insertion/oldest-first) default.
    List<Transaction> findAllByOrderByRequestedAtDesc();

    List<Transaction> findByMsisdnOrderByRequestedAtDesc(String msisdn);

    List<Transaction> findByStatusOrderByRequestedAtDesc(TransactionStatus status);

    List<Transaction> findByMsisdnAndStatusOrderByRequestedAtDesc(String msisdn, TransactionStatus status);

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
