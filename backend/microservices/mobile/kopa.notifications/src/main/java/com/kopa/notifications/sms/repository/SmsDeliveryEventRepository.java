package com.kopa.notifications.sms.repository;

import com.kopa.notifications.sms.model.SmsDeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsDeliveryEventRepository extends JpaRepository<SmsDeliveryEvent, String> {
}
