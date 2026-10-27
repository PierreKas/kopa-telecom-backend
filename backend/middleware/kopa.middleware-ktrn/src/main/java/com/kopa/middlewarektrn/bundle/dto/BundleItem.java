package com.kopa.middlewarektrn.bundle.dto;

/** One record from PROD_LIST: "PRODUCT CODE,PRODUCT NAME,PRODUCT PRICE". */
public record BundleItem(
        String productCode,
        String productName,
        String price
) {
}
