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

        SmsPurpose purpose = request.purpose() == OtpPurpose.PIN_RESET
                ? SmsPurpose.OTP_PIN_RESET : SmsPurpose.OTP_TRANSACTION;

        SmsMessage message = smsDispatchService.dispatch(
                request.msisdn(), purpose, SmsTemplates.OTP, SmsTemplates.otp(request.code()),
                request.correlationId(), request.requestedByService());

        boolean success = message.getStatus() == SmsMessageStatus.SENT;
        return new OtpSendResponse(success, message.getProviderMessageId(), message.getFailureDetail());
    }
}
