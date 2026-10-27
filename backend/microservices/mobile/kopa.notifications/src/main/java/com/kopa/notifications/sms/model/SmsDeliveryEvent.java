package com.kopa.notifications.sms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Append-only - InTouch's DLR can arrive as a sequence of intermediate statuses (P -> Q -> S ->
 *  D/U), not a single terminal callback (http_intouch_api_1.2.pdf s3). */
@Entity
@Table(name = "sms_delivery_events")
@Getter
@Setter
public class SmsDeliveryEvent {

    @Id
    private String id;

    @Column(name = "sms_message_id", nullable = false)
    private String smsMessageId;

    /** Raw InTouch DLR status letter: P, D, Q, E, S, U. */
    @Column(nullable = false, length = 2)
    private String status;

    @Column(name = "raw_payload")
    private String rawPayload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
