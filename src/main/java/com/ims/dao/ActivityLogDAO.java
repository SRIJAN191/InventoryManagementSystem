package com.ims.dao;

import com.ims.db.DatabaseConnection;
import com.ims.model.ActivityLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {
    public void save(String actionType, String subject, String details, String performedBy, LocalDateTime performedAt) {
        String sql = """
            INSERT INTO activity_logs(action_type, subject, details, performed_by, performed_at)
            VALUES(?, ?, ?, ?, ?)
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, actionType);
            statement.setString(2, subject);
            statement.setString(3, details);
            statement.setString(4, performedBy);
            statement.setString(5, performedAt.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save activity log", exception);
        }
    }

    public List<ActivityLogEntry> findRecent(int limit) {
        List<ActivityLogEntry> entries = new ArrayList<>();
        String sql = """
            SELECT log_id, action_type, subject, details, performed_by, performed_at
            FROM activity_logs
            ORDER BY performed_at DESC, log_id DESC
            LIMIT ?
            """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                entries.add(map(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load activity log", exception);
        }
        return entries;
    }

    private ActivityLogEntry map(ResultSet resultSet) throws SQLException {
        return new ActivityLogEntry(
            resultSet.getInt("log_id"),
            resultSet.getString("action_type"),
            resultSet.getString("subject"),
            resultSet.getString("details"),
            resultSet.getString("performed_by"),
            LocalDateTime.parse(resultSet.getString("performed_at"))
        );
    }
}
