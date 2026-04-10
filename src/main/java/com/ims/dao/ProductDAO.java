package com.ims.dao;

import com.ims.db.DatabaseConnection;
import com.ims.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> findAll(String search, String category) {
        String sql = """
            SELECT * FROM products
            WHERE (? = '' OR lower(product_name) LIKE lower(?)
                          OR lower(COALESCE(variant, '')) LIKE lower(?)
                          OR lower(COALESCE(color, '')) LIKE lower(?))
              AND (? = 'All' OR category = ?)
            ORDER BY product_name, COALESCE(variant, ''), COALESCE(color, '')
            """;
        List<Product> products = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String normalizedSearch = search == null ? "" : search.trim();
            String searchPattern = "%" + normalizedSearch + "%";
            statement.setString(1, normalizedSearch);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            statement.setString(5, category == null ? "All" : category);
            statement.setString(6, category == null ? "All" : category);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                products.add(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch products", exception);
        }
        return products;
    }

    public void save(Product product) {
        String sql = """
            INSERT INTO products(product_name, variant, color, category, cost_price, selling_price, quantity, minimum_stock)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getProductName());
            statement.setString(2, product.getVariant());
            statement.setString(3, product.getColor());
            statement.setString(4, product.getCategory());
            statement.setDouble(5, product.getCostPrice());
            statement.setDouble(6, product.getSellingPrice());
            statement.setInt(7, product.getQuantity());
            statement.setInt(8, product.getMinimumStock());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save product", exception);
        }
    }

    public void update(Product product) {
        String sql = """
            UPDATE products
            SET product_name = ?, variant = ?, color = ?, category = ?, cost_price = ?, selling_price = ?, quantity = ?, minimum_stock = ?
            WHERE product_id = ?
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getProductName());
            statement.setString(2, product.getVariant());
            statement.setString(3, product.getColor());
            statement.setString(4, product.getCategory());
            statement.setDouble(5, product.getCostPrice());
            statement.setDouble(6, product.getSellingPrice());
            statement.setInt(7, product.getQuantity());
            statement.setInt(8, product.getMinimumStock());
            statement.setInt(9, product.getProductId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update product", exception);
        }
    }

    public void delete(int productId) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM products WHERE product_id = ?")) {
            statement.setInt(1, productId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete product", exception);
        }
    }

    public Product findById(int productId) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM products WHERE product_id = ?")) {
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return map(resultSet);
            }
            return null;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch product", exception);
        }
    }

    private Product map(ResultSet resultSet) throws SQLException {
        return new Product(
            resultSet.getInt("product_id"),
            resultSet.getString("product_name"),
            resultSet.getString("variant"),
            resultSet.getString("color"),
            resultSet.getString("category"),
            resultSet.getDouble("cost_price"),
            resultSet.getDouble("selling_price"),
            resultSet.getInt("quantity"),
            resultSet.getInt("minimum_stock")
        );
    }
}
