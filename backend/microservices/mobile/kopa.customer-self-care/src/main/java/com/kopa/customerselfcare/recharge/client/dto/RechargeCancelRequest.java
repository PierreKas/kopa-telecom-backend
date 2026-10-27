package com.kopa.customerselfcare.recharge.client.dto;

public record RechargeCancelRequest(String msisdn, String nationalId, String ispCd, String approvalNo, String cancelAmount) {
}
