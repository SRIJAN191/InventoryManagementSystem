package com.ims.model;

import java.time.LocalDate;

public class Sale {
    private int saleId;
    private int productId;
    private String productName;
    private int quantity;
    private double revenue;
    private double profit;
    private LocalDate saleDate;

    public Sale() {
    }

    public Sale(int saleId, int productId, String productName, int quantity, double revenue,
                double profit, LocalDate saleDate) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.revenue = revenue;
        this.profit = profit;
        this.saleDate = saleDate;
    }

    public int getSaleId() {
        return saleId;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getProfit() {
        return profit;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}
