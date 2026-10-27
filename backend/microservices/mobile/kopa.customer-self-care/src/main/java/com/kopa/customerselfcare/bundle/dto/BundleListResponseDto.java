package com.kopa.customerselfcare.bundle.dto;

import java.util.List;

public record BundleListResponseDto(String msisdn, List<BundleDto> bundles) {
}
