package com.kopa.adminportal.mirror.service;

import com.kopa.adminportal.mirror.dto.SmsMessageDto;
import com.kopa.adminportal.mirror.model.SmsMessage;
import com.kopa.adminportal.mirror.repository.SmsMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SmsMessageLookupService {

    private final SmsMessageRepository smsMessages;

    public SmsMessageLookupService(SmsMessageRepository smsMessages) {
        this.smsMessages = smsMessages;
    }

    @Transactional(readOnly = true)
    public List<SmsMessageDto> search(String msisdn) {
        List<SmsMessage> results = (msisdn == null || msisdn.isBlank())
                ? smsMessages.findAll()
                : smsMessages.findByMsisdn(msisdn);
        return results.stream().map(this::toDto).toList();
    }

    private SmsMessageDto toDto(SmsMessage m) {
        return new SmsMessageDto(m.getId(), m.getMsisdn(), m.getPurpose().name(), m.getTemplateCode(),
                m.getProviderMessageId(), m.getStatus().name(), m.getCost(), m.getFailureLayer(),
                m.getFailureDetail(), m.getRequestedByService(), m.getCreatedAt(), m.getUpdatedAt());
    }
}
