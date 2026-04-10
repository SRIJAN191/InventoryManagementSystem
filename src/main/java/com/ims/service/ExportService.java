package com.ims.service;

import com.ims.db.DatabaseConnection;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExportService {
    private static final List<String> BACKUP_TABLES = List.of(
        "categories",
        "products",
        "users",
        "sales",
        "activity_logs"
    );

    public void exportCsv(Window owner, String defaultName, List<String> rows) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        chooser.setInitialFileName(defaultName);
        var file = chooser.showSaveDialog(owner);
        if (file != null) {
            Files.write(Path.of(file.toURI()), rows);
        }
    }

    public void exportDatabaseBackup(Window owner, String defaultName) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Backup Database");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL File", "*.sql"));
        chooser.setInitialFileName(defaultName);
        var file = chooser.showSaveDialog(owner);
        if (file != null) {
            Files.write(Path.of(file.toURI()), buildBackupRows());
        }
    }

    private List<String> buildBackupRows() throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("-- Inventory Management System MySQL backup");
        rows.add("SET FOREIGN_KEY_CHECKS=0;");
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            for (String tableName : BACKUP_TABLES) {
                appendTableDump(connection, tableName, rows);
            }
        } catch (SQLException exception) {
            throw new IOException("Unable to build database backup.", exception);
        }
        rows.add("SET FOREIGN_KEY_CHECKS=1;");
        return rows;
    }

    private void appendTableDump(Connection connection, String tableName, List<String> rows) throws SQLException {
        rows.add("");
        rows.add("DELETE FROM `" + tableName + "`;");
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + tableName + "`");
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                List<String> columns = new ArrayList<>();
                for (int index = 1; index <= columnCount; index++) {
                    columns.add("`" + metadata.getColumnName(index) + "`");
                    values.add(toSqlLiteral(resultSet.getObject(index)));
                }
                rows.add("INSERT INTO `" + tableName + "` (" + String.join(", ", columns) + ") VALUES ("
                    + String.join(", ", values) + ");");
            }
        }
    }

    private String toSqlLiteral(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "'" + value.toString()
            .replace("\\", "\\\\")
            .replace("'", "''")
            .replace("\r", "\\r")
            .replace("\n", "\\n") + "'";
    }
}
