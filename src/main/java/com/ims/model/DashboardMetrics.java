package com.ims.model;

public record DashboardMetrics(
    int totalProducts,
    double totalStockValue,
    double todaysSales,
    double todaysProfit,
    int lowStockItems
) {
}
