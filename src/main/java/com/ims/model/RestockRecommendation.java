package com.ims.model;

public record RestockRecommendation(
    String productLabel,
    String category,
    int currentStock,
    int minimumStock,
    int suggestedOrder,
    String urgency,
    double estimatedCost,
    double projectedRevenue,
    double projectedProfit
) {
    public boolean needsReorder() {
        return suggestedOrder > 0;
    }
}
