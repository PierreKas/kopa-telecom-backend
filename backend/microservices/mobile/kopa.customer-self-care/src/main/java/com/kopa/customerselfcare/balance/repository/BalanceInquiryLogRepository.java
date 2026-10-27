package com.kopa.customerselfcare.balance.repository;

import com.kopa.customerselfcare.balance.model.BalanceInquiryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BalanceInquiryLogRepository extends JpaRepository<BalanceInquiryLog, UUID> {
}
