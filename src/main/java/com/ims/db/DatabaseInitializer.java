package com.ims.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    category_id INT PRIMARY KEY AUTO_INCREMENT,
                    category_name VARCHAR(255) NOT NULL UNIQUE
                )
                ENGINE=InnoDB
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    product_id INT PRIMARY KEY AUTO_INCREMENT,
                    product_name VARCHAR(255) NOT NULL,
                    category VARCHAR(255) NOT NULL,
                    cost_price DECIMAL(10, 2) NOT NULL,
                    selling_price DECIMAL(10, 2) NOT NULL,
                    quantity INT NOT NULL,
                    minimum_stock INT NOT NULL DEFAULT 5
                )
                ENGINE=InnoDB
                """);
            ensureProductColumns(connection);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    sale_id INT PRIMARY KEY AUTO_INCREMENT,
                    product_id INT NOT NULL,
                    product_name VARCHAR(255) NOT NULL,
                    quantity INT NOT NULL,
                    revenue DECIMAL(10, 2) NOT NULL,
                    profit DECIMAL(10, 2) NOT NULL,
                    sale_date DATE NOT NULL,
                    FOREIGN KEY(product_id) REFERENCES products(product_id)
                )
                ENGINE=InnoDB
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL
                )
                ENGINE=InnoDB
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id INT PRIMARY KEY AUTO_INCREMENT,
                    action_type VARCHAR(100) NOT NULL,
                    subject VARCHAR(255) NOT NULL,
                    details TEXT NOT NULL,
                    performed_by VARCHAR(100) NOT NULL,
                    performed_at VARCHAR(40) NOT NULL
                )
                ENGINE=InnoDB
                """);
            normalizeOwnerAccounts(connection);
            seedDefaultUser(connection, "admin", "admin123", "Admin");
            seedDefaultUser(connection, "owner", "owner123", "Admin");
            seedDefaultUser(connection, "staff", "staff123", "Staff");
            seedDefaultCategory(connection, "Electronics");
            seedDefaultCategory(connection, "Accessories");
            seedDefaultCategory(connection, "Furniture");
            seedSampleProducts(connection);
            syncProductCategories(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to initialize database", exception);
        }
    }

    private static void normalizeOwnerAccounts(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "UPDATE users SET role = 'Admin' WHERE lower(role) = 'owner'")) {
            statement.executeUpdate();
        }
    }

    private static void seedDefaultUser(Connection connection, String username, String password, String role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            """
                INSERT INTO users(username, password, role)
                VALUES(?, ?, ?)
                ON DUPLICATE KEY UPDATE username = username
                """)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }

    private static void seedDefaultCategory(Connection connection, String categoryName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            """
                INSERT INTO categories(category_name)
                VALUES(?)
                ON DUPLICATE KEY UPDATE category_name = category_name
                """)) {
            statement.setString(1, categoryName);
            statement.executeUpdate();
        }
    }

    private static void seedSampleProducts(Connection connection) throws SQLException {
        String sql = """
            INSERT INTO products(product_name, variant, color, category, cost_price, selling_price, quantity, minimum_stock)
            SELECT ?, ?, ?, ?, ?, ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1 FROM products
                WHERE product_name = ?
                  AND COALESCE(variant, '') = ?
                  AND COALESCE(color, '') = ?
            )
            """;
        insertProduct(connection, sql, "Laptop", "", "", "Electronics", 550, 760, 12, 4);
        insertProduct(connection, sql, "Barcode Scanner", "", "", "Accessories", 45, 75, 8, 3);
        insertProduct(connection, sql, "Office Chair", "", "", "Furniture", 80, 135, 5, 2);
        insertProduct(connection, sql, "Thermal Printer", "", "", "Electronics", 110, 170, 3, 3);
    }

    private static void syncProductCategories(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
            INSERT INTO categories(category_name)
            SELECT DISTINCT trim(category)
            FROM products
            WHERE category IS NOT NULL
              AND trim(category) <> ''
            ON DUPLICATE KEY UPDATE category_name = category_name
            """)) {
            statement.executeUpdate();
        }
    }

    private static void ensureProductColumns(Connection connection) throws SQLException {
        if (!columnExists(connection, "products", "variant")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE products ADD COLUMN variant VARCHAR(255) NOT NULL DEFAULT ''");
            }
        }
        if (!columnExists(connection, "products", "color")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE products ADD COLUMN color VARCHAR(255) NOT NULL DEFAULT ''");
            }
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet resultSet = metadata.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    private static void insertProduct(Connection connection, String sql, String name, String variant, String color,
                                      String category, double costPrice, double sellingPrice, int quantity,
                                      int minimumStock) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, variant);
            statement.setString(3, color);
            statement.setString(4, category);
            statement.setDouble(5, costPrice);
            statement.setDouble(6, sellingPrice);
            statement.setInt(7, quantity);
            statement.setInt(8, minimumStock);
            statement.setString(9, name);
            statement.setString(10, variant);
            statement.setString(11, color);
            statement.executeUpdate();
        }
    }
}
