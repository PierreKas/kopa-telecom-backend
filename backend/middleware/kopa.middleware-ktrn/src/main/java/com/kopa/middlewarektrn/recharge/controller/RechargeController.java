package com.kopa.middlewarektrn.recharge.controller;

import com.kopa.middlewarektrn.recharge.dto.RechargeCancelRequest;
import com.kopa.middlewarektrn.recharge.dto.RechargeRequest;
import com.kopa.middlewarektrn.recharge.dto.RechargeResponse;
import com.kopa.middlewarektrn.recharge.dto.RechargeVoucherRequest;
import com.kopa.middlewarektrn.recharge.service.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/recharge")
@Tag(name = "KTRN Recharge", description = "HP00002/HPOP001 recharge, HPOP002 cancel, HPOP003 "
        + "voucher - internal, called only by kopa.customer-self-care.")
public class RechargeController {

    private final RechargeService rechargeService;

    public RechargeController(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    @PostMapping
    @Operation(summary = "Recharge by amount (HPOP001)")
    public ResponseEntity<RechargeResponse> recharge(@Valid @RequestBody RechargeRequest request) {
        return ResponseEntity.ok(rechargeService.recharge(request));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel a recharge (HPOP002)")
    public ResponseEntity<RechargeResponse> cancel(@Valid @RequestBody RechargeCancelRequest request) {
        return ResponseEntity.ok(rechargeService.cancel(request));
    }

    @PostMapping("/voucher")
    @Operation(summary = "Recharge by voucher number (HPOP003)")
    public ResponseEntity<RechargeResponse> rechargeByVoucher(@Valid @RequestBody RechargeVoucherRequest request) {
        return ResponseEntity.ok(rechargeService.rechargeByVoucher(request));
    }
}
