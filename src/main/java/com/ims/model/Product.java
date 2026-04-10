package com.ims.model;

import java.util.Locale;

public class Product {
    private int productId;
    private String productName;
    private String variant;
    private String color;
    private String category;
    private double costPrice;//data members
    private double sellingPrice;
    private int quantity;
    private int minimumStock;

    public Product() {
    }

    public Product(int productId, String productName, String variant, String color, String category,
                   double costPrice, double sellingPrice, int quantity, int minimumStock) {
        this.productId = productId;
        this.productName = productName;
        this.variant = variant;
        this.color = color;
        this.category = category;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
    }

    public String getDisplayName() {
        StringBuilder builder = new StringBuilder();
        if (productName != null) {
            builder.append(productName.trim());
        }
        if (variant != null && !variant.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" - ");
            }
            builder.append(variant.trim());
        }
        if (color != null && !color.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" - ");
            }
            builder.append(color.trim());
        }
        return builder.toString();
    }

    public boolean isBelowMinimumStock() {
        return quantity < minimumStock;
    }

    public double getStockValue() {
        return costPrice * quantity;
    }

    public double getUnitProfit() {
        return sellingPrice - costPrice;
    }

    public boolean matchesDisplayLabel(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return getDisplayName().toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }
}
