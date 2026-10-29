package com.kopa.adminportal.dashboard.service;

import com.kopa.adminportal.dashboard.dto.DashboardFailuresDto;
import com.kopa.adminportal.dashboard.dto.DashboardSummaryDto;
import com.kopa.adminportal.dashboard.dto.FailureBreakdownEntryDto;
import com.kopa.adminportal.mirror.model.SmsMessageStatus;
import com.kopa.adminportal.mirror.model.TransactionStatus;
import com.kopa.adminportal.mirror.repository.CustomerRepository;
import com.kopa.adminportal.mirror.repository.SmsMessageRepository;
import com.kopa.adminportal.mirror.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
public class DashboardService {

    private final CustomerRepository customers;
    private final TransactionRepository transactions;
    private final SmsMessageRepository smsMessages;

    public DashboardService(CustomerRepository customers, TransactionRepository transactions,
                             SmsMessageRepository smsMessages) {
        this.customers = customers;
        this.transactions = transactions;
        this.smsMessages = smsMessages;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto summary() {
        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS).atZone(ZoneOffset.UTC).toInstant();

        long registrations = customers.countByCreatedAtAfter(startOfToday);
        long txToday = transactions.countByRequestedAtAfter(startOfToday);
        long txSuccess = transactions.countByStatusAndRequestedAtAfter(TransactionStatus.SUCCESS, startOfToday);
        long txFailed = transactions.countByStatusAndRequestedAtAfter(TransactionStatus.FAILED, startOfToday);
        long smsSent = smsMessages.countByStatusAndCreatedAtAfter(SmsMessageStatus.SENT, startOfToday)
                + smsMessages.countByStatusAndCreatedAtAfter(SmsMessageStatus.DELIVERED, startOfToday);
        long smsFailed = smsMessages.countByStatusAndCreatedAtAfter(SmsMessageStatus.FAILED_TO_SEND, startOfToday)
                + smsMessages.countByStatusAndCreatedAtAfter(SmsMessageStatus.UNDELIVERED, startOfToday)
                + smsMessages.countByStatusAndCreatedAtAfter(SmsMessageStatus.ERRORED, startOfToday);
        long smsTotal = smsSent + smsFailed;
        double deliveryRate = smsTotal == 0 ? 100.0 : (smsSent * 100.0) / smsTotal;

        return new DashboardSummaryDto(registrations, txToday, txSuccess, txFailed, smsSent, smsFailed, deliveryRate);
    }

    @Transactional(readOnly = true)
    public DashboardFailuresDto failures() {
        var byLayer = transactions.countFailuresByLayer().stream()
                .map(row -> new FailureBreakdownEntryDto(
                        row.getFailureLayer() == null ? "UNKNOWN" : row.getFailureLayer(), row.getTotal()))
                .toList();
        long total = byLayer.stream().mapToLong(FailureBreakdownEntryDto::count).sum();
        return new DashboardFailuresDto(total, byLayer);
    }
}
