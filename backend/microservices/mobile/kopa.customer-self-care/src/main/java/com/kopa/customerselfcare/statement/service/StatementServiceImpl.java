package com.kopa.customerselfcare.statement.service;

import com.kopa.customerselfcare.balance.client.MiddlewareBalanceClient;
import com.kopa.customerselfcare.balance.client.dto.BundleHistoryEntryDto;
import com.kopa.customerselfcare.statement.dto.StatementEntryDto;
import com.kopa.customerselfcare.statement.dto.StatementResponseDto;
import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StatementServiceImpl implements StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementServiceImpl.class);

    private static final String SOURCE_LOCAL = "LOCAL";
    private static final String SOURCE_KTRN_HISTORY = "KTRN_HISTORY";
    private static final String CURRENCY = "RWF";

    // RBSS-CCBS API_ACCESS s2.1.5's BUNDLE_HIST dates have no timezone of their own - the KTRN
    // gateway is Kopa's own Rwanda-based system, so Africa/Kigali (UTC+2, no DST) is the right
    // zone to anchor them to for comparing against this service's own Instant-based timestamps.
    private static final DateTimeFormatter BUNDLE_HIST_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final ZoneId KTRN_ZONE = ZoneId.of("Africa/Kigali");

    private static final Comparator<String> BY_DATE_DESCENDING =
            Comparator.nullsLast(Comparator.<String>reverseOrder());

    private final TransactionRepository transactions;
    private final MiddlewareBalanceClient middlewareBalanceClient;

    public StatementServiceImpl(TransactionRepository transactions, MiddlewareBalanceClient middlewareBalanceClient) {
        this.transactions = transactions;
        this.middlewareBalanceClient = middlewareBalanceClient;
    }

    @Override
    public StatementResponseDto getStatement(String msisdn) {
        List<StatementEntryDto> entries = new ArrayList<>();
        transactions.findByMsisdnOrderByRequestedAtDesc(msisdn).forEach(t -> entries.add(toLocalEntry(t)));
        entries.addAll(fetchBundleHistory(msisdn));

        entries.sort(Comparator.comparing(StatementEntryDto::requestedAt, BY_DATE_DESCENDING));
        return new StatementResponseDto(msisdn, entries);
    }

    // BUNDLE_HIST enrichment is additive, not load-bearing - the local transactions table is the
    // statement's primary source of truth, so a KTRN/middleware outage degrades this to a
    // local-only statement instead of failing the whole request.
    private List<StatementEntryDto> fetchBundleHistory(String msisdn) {
        try {
            var summary = middlewareBalanceClient.checkSummary(msisdn);
            return summary.bundleHistory().stream().map(StatementServiceImpl::toHistoryEntry).toList();
        } catch (RuntimeException ex) {
            logger.warn("Could not enrich statement for {} with KTRN BUNDLE_HIST, returning local entries only: {}",
                    msisdn, ex.getMessage());
            return List.of();
        }
    }

    private static StatementEntryDto toLocalEntry(Transaction transaction) {
        return new StatementEntryDto(
                transaction.getId(),
                SOURCE_LOCAL,
                transaction.getTransactionType().name(),
                transaction.getProductCode(),
                transaction.getAmount() == null ? null : transaction.getAmount().toPlainString(),
                transaction.getCurrency(),
                transaction.getStatus().name(),
                transaction.getKtrnMessage(),
                transaction.getRequestedAt() == null ? null : transaction.getRequestedAt().toString(),
                transaction.getCompletedAt() == null ? null : transaction.getCompletedAt().toString(),
                null
        );
    }

    private static StatementEntryDto toHistoryEntry(BundleHistoryEntryDto entry) {
        String occurredAt = toInstantString(entry.dateTime());
        return new StatementEntryDto(
                null,
                SOURCE_KTRN_HISTORY,
                entry.action(),
                entry.productName(),
                entry.amount(),
                CURRENCY,
                // The doc doesn't define FLAG's semantics beyond "Y" in its own examples - passed
                // through raw rather than guessing at a LOCAL-style status like "COMPLETED".
                entry.flag(),
                null,
                occurredAt,
                occurredAt,
                toInstantString(entry.expiryDateTime())
        );
    }

    private static String toInstantString(String ktrnDateTime) {
        if (ktrnDateTime == null || ktrnDateTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(ktrnDateTime.trim(), BUNDLE_HIST_DATE_FORMAT)
                    .atZone(KTRN_ZONE).toInstant().toString();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
