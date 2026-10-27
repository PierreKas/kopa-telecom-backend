package com.kopa.middlewarektrn.bundle.controller;

import com.kopa.middlewarektrn.bundle.dto.BundleListRequest;
import com.kopa.middlewarektrn.bundle.dto.BundleListResponse;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseCheckRequest;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseCheckResponse;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseRequest;
import com.kopa.middlewarektrn.bundle.dto.BundlePurchaseResponse;
import com.kopa.middlewarektrn.bundle.dto.EWalletCheckRequest;
import com.kopa.middlewarektrn.bundle.dto.EWalletCheckResponse;
import com.kopa.middlewarektrn.bundle.service.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ktrn/bundles")
@Tag(name = "KTRN Bundles", description = "HP00001/HPOP005 list, HP00002/HPOP004 purchase, "
        + "HP00005/HPOP403 purchase-check, HP00005/HPOP402 ewallet-check - internal, called only "
        + "by kopa.customer-self-care.")
public class BundleController {

    private final BundleService bundleService;

    public BundleController(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    @PostMapping("/available")
    @Operation(summary = "List bundles available for purchase (HPOP005)")
    public ResponseEntity<BundleListResponse> listAvailable(@Valid @RequestBody BundleListRequest request) {
        return ResponseEntity.ok(bundleService.listAvailableBundles(request));
    }

    @PostMapping("/purchase")
    @Operation(summary = "Purchase a bundle (HPOP004)")
    public ResponseEntity<BundlePurchaseResponse> purchase(@Valid @RequestBody BundlePurchaseRequest request) {
        return ResponseEntity.ok(bundleService.purchase(request));
    }

    @PostMapping("/purchase-check")
    @Operation(summary = "Check whether a bundle purchase would succeed (HPOP403)")
    public ResponseEntity<BundlePurchaseCheckResponse> checkPurchase(@Valid @RequestBody BundlePurchaseCheckRequest request) {
        return ResponseEntity.ok(bundleService.checkPurchase(request));
    }

    @PostMapping("/ewallet-check")
    @Operation(summary = "Check e-wallet balance can cover a bundle purchase (HPOP402)")
    public ResponseEntity<EWalletCheckResponse> checkEWallet(@Valid @RequestBody EWalletCheckRequest request) {
        return ResponseEntity.ok(bundleService.checkEWallet(request));
    }
}
