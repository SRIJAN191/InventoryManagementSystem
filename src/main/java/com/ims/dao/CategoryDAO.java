package com.ims.dao;

import com.ims.db.DatabaseConnection;
import com.ims.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new Category(
                    resultSet.getInt("category_id"),
                    resultSet.getString("category_name")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch categories", exception);
        }
        return categories;
    }

    public List<String> findNames(boolean includeAll) {
        List<String> names = new ArrayList<>();
        if (includeAll) {
            names.add("All");
        }
        String sql = "SELECT category_name FROM categories ORDER BY category_name";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                names.add(resultSet.getString("category_name"));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load category names", exception);
        }
        return names;
    }

    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM categories WHERE lower(category_name) = lower(?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to validate category", exception);
        }
    }

    public void save(String categoryName) {
        String sql = "INSERT INTO categories(category_name) VALUES(?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoryName);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save category", exception);
        }
    }

    public boolean isInUse(String categoryName) {
        String sql = "SELECT COUNT(*) FROM products WHERE lower(category) = lower(?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoryName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify category usage", exception);
        }
    }

    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete category", exception);
        }
    }
}