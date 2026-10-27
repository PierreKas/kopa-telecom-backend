package com.kopa.notifications.sms.controller;

import com.kopa.notifications.sms.dto.DeliveryStatusRequest;
import com.kopa.notifications.sms.service.DeliveryStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/sms")
@Tag(name = "SMS Delivery Status", description = "Sync - receives a DLR event forwarded by "
        + "kopa.middleware-sms, never called directly by InTouch.")
public class DeliveryStatusController {

    private final DeliveryStatusService deliveryStatusService;

    public DeliveryStatusController(DeliveryStatusService deliveryStatusService) {
        this.deliveryStatusService = deliveryStatusService;
    }

    @PostMapping("/delivery-status")
    @Operation(summary = "Record a forwarded InTouch delivery report")
    public ResponseEntity<Void> deliveryStatus(@Valid @RequestBody DeliveryStatusRequest request) {
        deliveryStatusService.recordDeliveryStatus(request);
        return ResponseEntity.ok().build();
    }
}
