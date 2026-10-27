package com.kopa.customerselfcare.inquiry.repository;

import com.kopa.customerselfcare.inquiry.model.ServiceInquiryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInquiryLogRepository extends JpaRepository<ServiceInquiryLog, String> {
}
