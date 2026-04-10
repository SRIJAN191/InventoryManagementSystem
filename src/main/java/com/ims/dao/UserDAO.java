package com.ims.dao;

import com.ims.db.DatabaseConnection;
import com.ims.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User findByUsername(String username) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM users WHERE username = ?")) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("role")
                );
            }
            return null;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch user", exception);
        }
    }

    public List<User> findAllByRole(String role, String search) {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT user_id, username, password, role
            FROM users
            WHERE lower(role) = lower(?)
              AND lower(username) LIKE lower(?)
            ORDER BY username
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            statement.setString(2, "%" + (search == null ? "" : search.trim()) + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("role")
                ));
            }
            return users;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load users", exception);
        }
    }

    public boolean existsByUsername(String username, Integer excludeUserId) {
        String sql = """
            SELECT 1
            FROM users
            WHERE lower(username) = lower(?)
              AND (? IS NULL OR user_id <> ?)
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            if (excludeUserId == null) {
                statement.setObject(2, null);
                statement.setObject(3, null);
            } else {
                statement.setInt(2, excludeUserId);
                statement.setInt(3, excludeUserId);
            }
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to validate username", exception);
        }
    }

    public void save(User user) {
        String sql = "INSERT INTO users(username, password, role) VALUES(?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRole());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save user", exception);
        }
    }

    public void update(User user, boolean updatePassword) {
        String sql = updatePassword
            ? "UPDATE users SET username = ?, password = ?, role = ? WHERE user_id = ?"
            : "UPDATE users SET username = ?, role = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            if (updatePassword) {
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getRole());
                statement.setInt(4, user.getUserId());
            } else {
                statement.setString(2, user.getRole());
                statement.setInt(3, user.getUserId());
            }
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update user", exception);
        }
    }

    public void delete(int userId) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to delete user", exception);
        }
    }
}
