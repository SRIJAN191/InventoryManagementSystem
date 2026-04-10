package com.ims.service;

import com.ims.dao.CategoryDAO;
import com.ims.dao.ProductDAO;
import com.ims.dao.SaleDAO;
import com.ims.db.DatabaseConnection;
import com.ims.model.Category;
import com.ims.model.Product;
import com.ims.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ValidationService validationService = new ValidationService();

    public List<Product> getProducts(String search, String category) {
        return productDAO.findAll(search, category);
    }

    public List<String> getCategories() {
        return categoryDAO.findNames(true);
    }

    public List<String> getCategoryNames() {
        return categoryDAO.findNames(false);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public void addCategory(String categoryName) {
        String normalizedName = categoryName == null ? "" : categoryName.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        if (categoryDAO.existsByName(normalizedName)) {
            throw new IllegalArgumentException("Duplicate category names are not allowed.");
        }
        categoryDAO.save(normalizedName);
    }

    public void deleteCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Select a category to delete.");
        }
        if (categoryDAO.isInUse(category.categoryName())) {
            throw new IllegalArgumentException("This category is assigned to one or more products.");
        }
        categoryDAO.delete(category.categoryId());
    }

    public void saveProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Add at least one variant row before saving.");
        }
        List<String> errors = new ArrayList<>();
        for (Product product : products) {
            errors.addAll(validationService.validateProduct(product));
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(System.lineSeparator(), errors));
        }
        for (Product product : products) {
            if (product.getProductId() > 0) {
                productDAO.update(product);
            } else {
                productDAO.save(product);
            }
        }
    }

    public void deleteProduct(Product product) {
        if (product == null) {
            return;
        }
        if (saleDAO.existsForProduct(product.getProductId())) {
            throw new IllegalArgumentException("Products with recorded sales history cannot be deleted.");
        }
        productDAO.delete(product.getProductId());
    }

    public Sale recordSale(Product product, int quantity) {
        List<String> errors = validationService.validateSale(product, quantity);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(System.lineSeparator(), errors));
        }
        double revenue = product.getSellingPrice() * quantity;
        double profit = (product.getSellingPrice() - product.getCostPrice()) * quantity;
        Sale sale = new Sale(0, product.getProductId(), product.getDisplayName(), quantity, revenue, profit, LocalDate.now());
        runSaleTransaction(
            """
                INSERT INTO sales(product_id, product_name, quantity, revenue, profit, sale_date)
                VALUES(?, ?, ?, ?, ?, ?)
                """,
            sale,
            product.getQuantity() - quantity
        );
        return sale;
    }

    public void removeSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Select a sale to remove.");
        }

        Product product = productDAO.findById(sale.getProductId());
        if (product == null) {
            throw new IllegalStateException("The linked product no longer exists, so stock cannot be restored.");
        }

        runSaleRemovalTransaction(sale, product.getQuantity() + sale.getQuantity());
    }

    public Sale updateSale(Sale originalSale, Product product, int quantity) {
        if (originalSale == null) {
            throw new IllegalArgumentException("Select a sale to edit.");
        }
        if (product == null) {
            throw new IllegalArgumentException("Please select a product to sell.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sale quantity must be greater than 0.");
        }

        Product latestSelectedProduct = productDAO.findById(product.getProductId());
        if (latestSelectedProduct == null) {
            throw new IllegalStateException("The selected product no longer exists.");
        }

        boolean sameProduct = latestSelectedProduct.getProductId() == originalSale.getProductId();
        int availableStock = latestSelectedProduct.getQuantity() + (sameProduct ? originalSale.getQuantity() : 0);
        if (quantity > availableStock) {
            throw new IllegalArgumentException("Cannot sell more than the available stock.");
        }

        double revenue = latestSelectedProduct.getSellingPrice() * quantity;
        double profit = (latestSelectedProduct.getSellingPrice() - latestSelectedProduct.getCostPrice()) * quantity;
        Sale updatedSale = new Sale(
            originalSale.getSaleId(),
            latestSelectedProduct.getProductId(),
            latestSelectedProduct.getDisplayName(),
            quantity,
            revenue,
            profit,
            originalSale.getSaleDate()
        );

        Product originalProduct = sameProduct ? latestSelectedProduct : productDAO.findById(originalSale.getProductId());
        if (originalProduct == null) {
            throw new IllegalStateException("The original product no longer exists, so the sale cannot be edited.");
        }

        runSaleUpdateTransaction(originalSale, updatedSale, originalProduct, latestSelectedProduct, sameProduct);
        return updatedSale;
    }

    private void runSaleTransaction(String insertSql, Sale sale, int updatedStock) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insertSale = connection.prepareStatement(insertSql);
                 PreparedStatement updateStock = connection.prepareStatement(
                     "UPDATE products SET quantity = ? WHERE product_id = ?")) {
                insertSale.setInt(1, sale.getProductId());
                insertSale.setString(2, sale.getProductName());
                insertSale.setInt(3, sale.getQuantity());
                insertSale.setDouble(4, sale.getRevenue());
                insertSale.setDouble(5, sale.getProfit());
                insertSale.setString(6, sale.getSaleDate().toString());
                insertSale.executeUpdate();

                updateStock.setInt(1, updatedStock);
                updateStock.setInt(2, sale.getProductId());
                updateStock.executeUpdate();

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save sale", exception);
        }
    }

    private void runSaleRemovalTransaction(Sale sale, int updatedStock) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStock = connection.prepareStatement(
                     "UPDATE products SET quantity = ? WHERE product_id = ?");
                 PreparedStatement deleteSale = connection.prepareStatement(
                     "DELETE FROM sales WHERE sale_id = ?")) {
                updateStock.setInt(1, updatedStock);
                updateStock.setInt(2, sale.getProductId());
                updateStock.executeUpdate();

                deleteSale.setInt(1, sale.getSaleId());
                deleteSale.executeUpdate();

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to remove sale", exception);
        }
    }

    private void runSaleUpdateTransaction(
        Sale originalSale,
        Sale updatedSale,
        Product originalProduct,
        Product selectedProduct,
        boolean sameProduct
    ) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateProductStock = connection.prepareStatement(
                     "UPDATE products SET quantity = ? WHERE product_id = ?");
                 PreparedStatement updateSale = connection.prepareStatement("""
                     UPDATE sales
                     SET product_id = ?, product_name = ?, quantity = ?, revenue = ?, profit = ?, sale_date = ?
                     WHERE sale_id = ?
                     """)) {
                if (sameProduct) {
                    int adjustedStock = selectedProduct.getQuantity() + originalSale.getQuantity() - updatedSale.getQuantity();
                    updateProductStock.setInt(1, adjustedStock);
                    updateProductStock.setInt(2, selectedProduct.getProductId());
                    updateProductStock.executeUpdate();
                } else {
                    updateProductStock.setInt(1, originalProduct.getQuantity() + originalSale.getQuantity());
                    updateProductStock.setInt(2, originalProduct.getProductId());
                    updateProductStock.executeUpdate();

                    updateProductStock.setInt(1, selectedProduct.getQuantity() - updatedSale.getQuantity());
                    updateProductStock.setInt(2, selectedProduct.getProductId());
                    updateProductStock.executeUpdate();
                }

                updateSale.setInt(1, updatedSale.getProductId());
                updateSale.setString(2, updatedSale.getProductName());
                updateSale.setInt(3, updatedSale.getQuantity());
                updateSale.setDouble(4, updatedSale.getRevenue());
                updateSale.setDouble(5, updatedSale.getProfit());
                updateSale.setString(6, updatedSale.getSaleDate().toString());
                updateSale.setInt(7, updatedSale.getSaleId());
                updateSale.executeUpdate();

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update sale", exception);
        }
    }
}
