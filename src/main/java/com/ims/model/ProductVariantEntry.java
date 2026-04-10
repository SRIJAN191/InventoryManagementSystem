package com.ims.model;

public class ProductVariantEntry {
    private int productId;
    private String variant;
    private String color;
    private double costPrice;
    private double sellingPrice;
    private int quantity;
    private int minimumStock;

    public ProductVariantEntry(int productId, String variant, String color, double costPrice,
                               double sellingPrice, int quantity, int minimumStock) {
        this.productId = productId;
        this.variant = variant;
        this.color = color;
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

    public Product toProduct(String productName, String category) {
        return new Product(
            productId,
            productName,
            variant,
            color,
            category,
            costPrice,
            sellingPrice,
            quantity,
            minimumStock
        );
    }

    public static ProductVariantEntry fromProduct(Product product) {
        return new ProductVariantEntry(
            product.getProductId(),
            product.getVariant(),
            product.getColor(),
            product.getCostPrice(),
            product.getSellingPrice(),
            product.getQuantity(),
            product.getMinimumStock()
        );
    }
}
