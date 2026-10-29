package com.kopa.adminportal.mirror.service;

import com.kopa.adminportal.common.exception.NotFoundException;
import com.kopa.adminportal.mirror.dto.TransactionDto;
import com.kopa.adminportal.mirror.model.Transaction;
import com.kopa.adminportal.mirror.model.TransactionStatus;
import com.kopa.adminportal.mirror.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            results = transactions.findByMsisdnAndStatus(msisdn, parsedStatus);
        } else if (hasMsisdn) {
            results = transactions.findByMsisdn(msisdn);
        } else if (parsedStatus != null) {
            results = transactions.findByStatus(parsedStatus);
        } else {
            results = transactions.findAll();
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
                t.getAmount(), t.getCurrency(), t.getStatus().name(), t.getKtrnMsgcode(), t.getKtrnMessage(),
                t.getFailureLayer(), t.getFailureDetail(), t.getRequestedAt(), t.getCompletedAt(), t.getDurationMs());
    }
}
