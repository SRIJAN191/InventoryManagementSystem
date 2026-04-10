package com.ims.service;

import com.ims.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ValidationService {
    public List<String> validateProduct(Product product) {
        List<String> errors = new ArrayList<>();
        if (product.getProductName() == null || product.getProductName().isBlank()) {
            errors.add("Product name cannot be empty.");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            errors.add("Category is required.");
        }
        if (product.getCostPrice() <= 0) {
            errors.add("Cost price must be greater than 0.");
        }
        if (product.getSellingPrice() <= product.getCostPrice()) {
            errors.add("Selling price must be greater than cost price.");
        }
        if (product.getQuantity() < 0) {
            errors.add("Quantity cannot be negative.");
        }
        if (product.getMinimumStock() < 0) {
            errors.add("Minimum stock cannot be negative.");
        }
        return errors;
    }

    public List<String> validateSale(Product product, int quantity) {
        List<String> errors = new ArrayList<>();
        if (product == null) {
            errors.add("Please select a product to sell.");
        }
        if (quantity <= 0) {
            errors.add("Sale quantity must be greater than 0.");
        }
        if (product != null && quantity > product.getQuantity()) {
            errors.add("Cannot sell more than the available stock.");
        }
        return errors;
    }
}
