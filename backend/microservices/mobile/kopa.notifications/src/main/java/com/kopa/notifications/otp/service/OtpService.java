package com.kopa.notifications.otp.service;

import com.kopa.notifications.otp.dto.OtpPurpose;
import com.kopa.notifications.otp.dto.OtpSendRequest;
import com.kopa.notifications.otp.dto.OtpSendResponse;
import com.kopa.notifications.sms.model.SmsMessage;
import com.kopa.notifications.sms.model.SmsMessageStatus;
import com.kopa.notifications.sms.model.SmsPurpose;
import com.kopa.notifications.sms.service.SmsDispatchService;
import com.kopa.notifications.sms.service.SmsTemplates;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final OtpRateLimitService rateLimitService;
    private final SmsDispatchService smsDispatchService;

    public OtpService(OtpRateLimitService rateLimitService, SmsDispatchService smsDispatchService) {
        this.rateLimitService = rateLimitService;
        this.smsDispatchService = smsDispatchService;
    }

    public OtpSendResponse send(OtpSendRequest request) {
        if (!rateLimitService.tryConsume(request.msisdn())) {
            return new OtpSendResponse(false, null, "Too many OTP requests for this number - try again later");
        }

        SmsPurpose purpose;
        String templateCode;
        String renderedMessage;
        switch (request.purpose()) {
            case PIN_RESET -> {
                purpose = SmsPurpose.OTP_PIN_RESET;
                templateCode = SmsTemplates.OTP;
                renderedMessage = SmsTemplates.otp(request.customerFullName(), request.code());
            }
            case INITIAL_PIN -> {
                purpose = SmsPurpose.INITIAL_PIN;
                templateCode = SmsTemplates.INITIAL_PIN;
                renderedMessage = SmsTemplates.initialPin(request.customerFullName(), request.code());
            }
            default -> {
                purpose = SmsPurpose.OTP_TRANSACTION;
                templateCode = SmsTemplates.OTP;
                renderedMessage = SmsTemplates.otp(request.customerFullName(), request.code());
            }
        }

        SmsMessage message = smsDispatchService.dispatch(
                request.msisdn(), purpose, templateCode, renderedMessage,
                request.correlationId(), request.requestedByService());

        boolean success = message.getStatus() == SmsMessageStatus.SENT;
        return new OtpSendResponse(success, message.getProviderMessageId(), message.getFailureDetail());
    }
}
