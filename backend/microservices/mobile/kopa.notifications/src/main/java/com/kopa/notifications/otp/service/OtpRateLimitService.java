package com.kopa.notifications.otp.service;

import com.kopa.notifications.sms.model.OtpRateLimit;
import com.kopa.notifications.sms.repository.OtpRateLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Fixed-window limiter: at most MAX_PER_WINDOW OTP sends per msisdn per WINDOW. Protects against
 *  a caller bug spamming SMS cost, not a security control (callers are our own internal services,
 *  already gated by X-Internal-Api-Key). */
@Service
public class OtpRateLimitService {

    private static final int MAX_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofHours(1);

    private final OtpRateLimitRepository repository;

    public OtpRateLimitService(OtpRateLimitRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean tryConsume(String msisdn) {
        Instant now = Instant.now();
        OtpRateLimit limit = repository.findByMsisdn(msisdn).orElseGet(() -> {
            OtpRateLimit fresh = new OtpRateLimit();
            fresh.setId(UUID.randomUUID().toString());
            fresh.setMsisdn(msisdn);
            fresh.setWindowStart(now);
            fresh.setCount(0);
            return fresh;
        });

        if (Duration.between(limit.getWindowStart(), now).compareTo(WINDOW) >= 0) {
            limit.setWindowStart(now);
            limit.setCount(0);
        }

        if (limit.getCount() >= MAX_PER_WINDOW) {
            repository.save(limit);
            return false;
        }

        limit.setCount(limit.getCount() + 1);
        repository.save(limit);
        return true;
    }
}
