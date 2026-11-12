package com.kopa.customerselfcare.statement.service;

import com.kopa.customerselfcare.balance.client.MiddlewareBalanceClient;
import com.kopa.customerselfcare.balance.client.dto.BundleHistoryEntryDto;
import com.kopa.customerselfcare.statement.dto.StatementEntryDto;
import com.kopa.customerselfcare.statement.dto.StatementResponseDto;
import com.kopa.customerselfcare.transaction.model.Transaction;
import com.kopa.customerselfcare.transaction.model.TransactionStatus;
import com.kopa.customerselfcare.transaction.model.TransactionType;
import com.kopa.customerselfcare.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
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

    // This app never sells a bundle itself - every completed BUNDLE_PURCHASE also lands in
    // KTRN's own BUNDLE_HIST, so the two are two views of the same one event, not independent
    // transactions. There's no shared id to join them on, so entries within this window of each
    // other with the same amount are treated as the same purchase and the KTRN_HISTORY side is
    // dropped, keeping the richer LOCAL entry (it carries the transactionId/OTP status).
    private static final long DEDUP_WINDOW_MINUTES = 15;
    private static final String HISTORY_ACTION_PURCHASE = "PURCHASE";

    private final TransactionRepository transactions;
    private final MiddlewareBalanceClient middlewareBalanceClient;

    public StatementServiceImpl(TransactionRepository transactions, MiddlewareBalanceClient middlewareBalanceClient) {
        this.transactions = transactions;
        this.middlewareBalanceClient = middlewareBalanceClient;
    }

    @Override
    public StatementResponseDto getStatement(String msisdn) {
        List<Transaction> localTransactions = transactions.findByMsisdnOrderByRequestedAtDesc(msisdn);
        List<StatementEntryDto> entries = new ArrayList<>();
        localTransactions.forEach(t -> entries.add(toLocalEntry(t)));

        List<Transaction> completedBundlePurchases = localTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.BUNDLE_PURCHASE)
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .toList();

        fetchBundleHistory(msisdn).stream()
                .filter(entry -> !matchesExistingPurchase(entry, completedBundlePurchases))
                .map(StatementServiceImpl::toHistoryEntry)
                .forEach(entries::add);

        entries.sort(Comparator.comparing(StatementEntryDto::requestedAt, BY_DATE_DESCENDING));
        return new StatementResponseDto(msisdn, entries);
    }

    // BUNDLE_HIST enrichment is additive, not load-bearing - the local transactions table is the
    // statement's primary source of truth, so a KTRN/middleware outage degrades this to a
    // local-only statement instead of failing the whole request.
    private List<BundleHistoryEntryDto> fetchBundleHistory(String msisdn) {
        try {
            var summary = middlewareBalanceClient.checkSummary(msisdn);
            return summary.bundleHistory();
        } catch (RuntimeException ex) {
            logger.warn("Could not enrich statement for {} with KTRN BUNDLE_HIST, returning local entries only: {}",
                    msisdn, ex.getMessage());
            return List.of();
        }
    }

    private static boolean matchesExistingPurchase(BundleHistoryEntryDto entry, List<Transaction> completedBundlePurchases) {
        if (!HISTORY_ACTION_PURCHASE.equalsIgnoreCase(entry.action())) {
            return false;
        }
        Instant occurredAt = parseInstant(entry.dateTime());
        BigDecimal historyAmount = parseAmount(entry.amount());
        if (occurredAt == null || historyAmount == null) {
            return false;
        }
        return completedBundlePurchases.stream().anyMatch(t -> {
            if (t.getAmount() == null || historyAmount.compareTo(t.getAmount()) != 0) {
                return false;
            }
            Instant reference = t.getCompletedAt() != null ? t.getCompletedAt() : t.getRequestedAt();
            return reference != null
                    && Duration.between(reference, occurredAt).abs().toMinutes() <= DEDUP_WINDOW_MINUTES;
        });
    }

    private static BigDecimal parseAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(amount.trim());
        } catch (NumberFormatException ex) {
            return null;
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
                displayStatus(transaction),
                transaction.getKtrnMessage(),
                transaction.getRequestedAt() == null ? null : transaction.getRequestedAt().toString(),
                transaction.getCompletedAt() == null ? null : transaction.getCompletedAt().toString(),
                null
        );
    }

    // A customer who never retries after the OTP expires leaves the row at OTP_PENDING in the
    // DB - TransactionOtpService.verify() only flips it to EXPIRED the next time verify() runs
    // against it, which may never happen again. Compute the same outcome here so the statement
    // reflects reality immediately, without needing a backfill sweep.
    private static String displayStatus(Transaction transaction) {
        if (transaction.getStatus() == TransactionStatus.OTP_PENDING && transaction.isOtpExpired(Instant.now())) {
            return TransactionStatus.EXPIRED.name();
        }
        return transaction.getStatus().name();
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

    private static Instant parseInstant(String ktrnDateTime) {
        if (ktrnDateTime == null || ktrnDateTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(ktrnDateTime.trim(), BUNDLE_HIST_DATE_FORMAT).atZone(KTRN_ZONE).toInstant();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String toInstantString(String ktrnDateTime) {
        Instant instant = parseInstant(ktrnDateTime);
        return instant == null ? null : instant.toString();
    }
}
