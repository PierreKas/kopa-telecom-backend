package com.kopa.customerselfcare.recharge.client.dto;

public record RechargeRequest(String msisdn, String nationalId, String ispCd, String approvalNo, String amount) {
}
