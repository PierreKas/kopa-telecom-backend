package com.kopa.adminportal.mirror.repository;

import com.kopa.adminportal.mirror.model.SmsMessage;
import com.kopa.adminportal.mirror.model.SmsMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SmsMessageRepository extends JpaRepository<SmsMessage, String> {

    List<SmsMessage> findByMsisdn(String msisdn);

    long countByCreatedAtAfter(Instant since);

    long countByStatusAndCreatedAtAfter(SmsMessageStatus status, Instant since);
}
