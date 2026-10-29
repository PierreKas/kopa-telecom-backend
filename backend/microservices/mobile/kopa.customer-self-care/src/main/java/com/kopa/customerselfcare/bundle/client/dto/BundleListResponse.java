package com.kopa.customerselfcare.bundle.client.dto;

import java.util.List;

public record BundleListResponse(String msgcode, String message, List<BundleItem> items) {
}
