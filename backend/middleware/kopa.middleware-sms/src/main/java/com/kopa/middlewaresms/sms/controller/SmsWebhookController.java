package com.kopa.middlewaresms.sms.controller;

import com.kopa.middlewaresms.sms.service.DeliveryReportForwarder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The one endpoint on this whole platform reachable from the public internet - InTouch calls
 *  this directly (http_intouch_api_1.2.pdf s3), not one of our own services. Needs its own
 *  ingress rule, separate from the VPN-only KTRN traffic and the internal-only /internal/v1/**
 *  surface everywhere else (architecture-reference.md s7.2). */
@RestController
@Tag(name = "InTouch Delivery Webhook", description = "Public - InTouch calls this with the final "
        + "status of a previously-sent SMS. Always answers 200 OK per the InTouch spec, regardless "
        + "of whether forwarding to kopa.notifications succeeds.")
public class SmsWebhookController {

    private final DeliveryReportForwarder forwarder;

    public SmsWebhookController(DeliveryReportForwarder forwarder) {
        this.forwarder = forwarder;
    }

    @GetMapping("/webhooks/intouch/dlr")
    @Operation(summary = "InTouch delivery report callback")
    public ResponseEntity<Void> deliveryReport(@RequestParam("messageid") String messageId,
                                                @RequestParam("status") String status) {
        forwarder.forward(messageId, status);
        return ResponseEntity.ok().build();
    }
}
