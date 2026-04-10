package com.ims.model;

public record InventoryReportRow(
    String product,
    int stock,
    double costPrice,
    double totalValue
) {
}
