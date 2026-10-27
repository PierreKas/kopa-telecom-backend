package com.kopa.notifications.sms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Fixed-window counter protecting /internal/v1/otp/send from runaway SMS cost - one row per
 *  msisdn, reset by window_start. Does not own OTP code state itself (see OtpSendRequest javadoc). */
@Entity
@Table(name = "otp_rate_limit")
@Getter
@Setter
public class OtpRateLimit {

    @Id
    private String id;

    @Column(nullable = false, length = 15, unique = true)
    private String msisdn;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(nullable = false)
    private int count;
}
