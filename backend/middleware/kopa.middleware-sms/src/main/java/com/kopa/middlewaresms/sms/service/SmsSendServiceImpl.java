package com.kopa.middlewaresms.sms.service;

import com.kopa.middlewaresms.sms.dto.SendSmsRequest;
import com.kopa.middlewaresms.sms.dto.SendSmsResponse;
import com.kopa.middlewaresms.sms.gateway.IntouchSendResult;
import com.kopa.middlewaresms.sms.gateway.IntouchSmsClient;
import org.springframework.stereotype.Service;

@Service
public class SmsSendServiceImpl implements SmsSendService {

    private final IntouchSmsClient intouchSmsClient;

    public SmsSendServiceImpl(IntouchSmsClient intouchSmsClient) {
        this.intouchSmsClient = intouchSmsClient;
    }

    @Override
    public SendSmsResponse send(SendSmsRequest request) {
        IntouchSendResult result = intouchSmsClient.send(
                request.recipients(), request.message(), request.sender(), request.dlrUrl());

        return new SendSmsResponse(result.success(), result.messageId(), result.status(),
                result.cost(), result.balance(), result.errorMessage());
    }
}
