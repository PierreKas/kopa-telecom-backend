package com.kopa.notifications.sms.repository;

import com.kopa.notifications.sms.model.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsMessageRepository extends JpaRepository<SmsMessage, String> {

    Optional<SmsMessage> findByProviderMessageId(String providerMessageId);
}
