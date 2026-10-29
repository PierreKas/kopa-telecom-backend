package com.kopa.adminportal.dashboard.dto;

public record DashboardSummaryDto(
        long newRegistrationsToday,
        long transactionsToday,
        long successfulTransactionsToday,
        long failedTransactionsToday,
        long smsSentToday,
        long smsFailedToday,
        /** Percentage, 0-100. 100 when no SMS was sent today, to avoid a misleading 0%. */
        double smsDeliveryRateToday
) {
}
