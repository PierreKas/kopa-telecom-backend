package com.kopa.middlewaresms.sms.service;

import com.kopa.middlewaresms.sms.dto.SendSmsRequest;
import com.kopa.middlewaresms.sms.dto.SendSmsResponse;

public interface SmsSendService {

    SendSmsResponse send(SendSmsRequest request);
}
