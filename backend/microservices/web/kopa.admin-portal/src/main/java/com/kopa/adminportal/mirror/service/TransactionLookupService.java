package com.kopa.adminportal.mirror.service;

import com.kopa.adminportal.common.exception.NotFoundException;
import com.kopa.adminportal.mirror.dto.TransactionDto;
import com.kopa.adminportal.mirror.model.Transaction;
import com.kopa.adminportal.mirror.model.TransactionStatus;
import com.kopa.adminportal.mirror.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TransactionLookupService {

    private final TransactionRepository transactions;

    public TransactionLookupService(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> search(String msisdn, String status) {
        TransactionStatus parsedStatus = status == null || status.isBlank() ? null : TransactionStatus.valueOf(status);
        List<Transaction> results;
        boolean hasMsisdn = msisdn != null && !msisdn.isBlank();

        if (hasMsisdn && parsedStatus != null) {
            results = transactions.findByMsisdnAndStatusOrderByRequestedAtDesc(msisdn, parsedStatus);
        } else if (hasMsisdn) {
            results = transactions.findByMsisdnOrderByRequestedAtDesc(msisdn);
        } else if (parsedStatus != null) {
            results = transactions.findByStatusOrderByRequestedAtDesc(parsedStatus);
        } else {
            results = transactions.findAllByOrderByRequestedAtDesc();
        }
        return results.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TransactionDto getById(String id) {
        return transactions.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("No such transaction."));
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getMsisdn(), t.getTransactionType().name(), t.getProductCode(),
                t.getAmount(), t.getCurrency(), displayStatus(t), t.getKtrnMsgcode(), t.getKtrnMessage(),
                t.getFailureLayer(), t.getFailureDetail(), t.getRequestedAt(), t.getCompletedAt(), t.getDurationMs());
    }

    // A customer who never retries after the OTP expires leaves the row at OTP_PENDING in the
    // DB - kopa.customer-self-care's TransactionOtpService.verify() only flips it to EXPIRED the
    // next time verify() runs against it, which may never happen again. Compute the same outcome
    // here too (see StatementServiceImpl's identical reasoning on the mobile side) so the admin
    // portal reflects reality immediately, without needing a backfill sweep.
    private static String displayStatus(Transaction t) {
        if (t.getStatus() == TransactionStatus.OTP_PENDING && t.isOtpExpired(Instant.now())) {
            return TransactionStatus.EXPIRED.name();
        }
        return t.getStatus().name();
    }
}
