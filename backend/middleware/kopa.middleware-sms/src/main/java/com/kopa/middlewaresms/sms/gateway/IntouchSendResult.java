package com.kopa.middlewaresms.sms.gateway;

/** Our own normalized shape - InTouch's own success/failure JSON bodies have genuinely different
 *  structures (confirmed live 2026-10-13: success is {"details":[...],"summary":{...}}, failure is
 *  {"response":[{"errors":{...}}]}), so {@link IntouchSmsClient} parses both into this one record.
 *  {@code balance} is a String, not a number - InTouch's own summary.balance comes back
 *  comma-formatted ("17,243"), not a parseable double, and nothing here does math on it. */
public record IntouchSendResult(
        boolean success,
        String messageId,
        String status,
        Double cost,
        String balance,
        String errorMessage
) {
}
