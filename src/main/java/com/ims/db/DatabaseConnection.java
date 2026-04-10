package com.ims.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static DatabaseConnection instance;
    private static final Path CONFIG_PATH = Path.of(System.getProperty("user.dir"), "db.properties");
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "ims";
    private static final String DEFAULT_USERNAME = "root";
    private final String url;
    private final String username;
    private final String password;
    private final String databaseName;
    private final String host;
    private final String port;
    private final Properties fileProperties;
    private final Settings settings;

    private DatabaseConnection() {
        fileProperties = loadFileProperties();
        String configuredUrl = readSetting("IMS_DB_URL", "ims.db.url", "");
        host = readSetting("IMS_DB_HOST", "ims.db.host", DEFAULT_HOST);
        port = readSetting("IMS_DB_PORT", "ims.db.port", DEFAULT_PORT);
        databaseName = readSetting("IMS_DB_NAME", "ims.db.name", DEFAULT_DATABASE);
        username = readSetting("IMS_DB_USER", "ims.db.user", DEFAULT_USERNAME);
        password = readSetting("IMS_DB_PASSWORD", "ims.db.password", "");
        url = configuredUrl.isBlank() ? buildUrl(host, port, databaseName) : configuredUrl;
        settings = new Settings(host, port, databaseName, username, password, url);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Settings getSettings() {
        return settings;
    }

    public String getConfigLocation() {
        return CONFIG_PATH.toString();
    }

    public static synchronized void reset() {
        instance = null;
    }

    public static synchronized void saveSettings(Settings settings) {
        Properties properties = new Properties();
        properties.setProperty("ims.db.host", settings.host());
        properties.setProperty("ims.db.port", settings.port());
        properties.setProperty("ims.db.name", settings.databaseName());
        properties.setProperty("ims.db.user", settings.username());
        properties.setProperty("ims.db.password", settings.password());

        try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(outputStream, "Inventory Management System database settings");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write database config file: " + CONFIG_PATH, exception);
        }
    }

    public static Settings defaultSettings() {
        return new Settings(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_DATABASE, DEFAULT_USERNAME, "", "");
    }

    private String readSetting(String envName, String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String fileValue = fileProperties.getProperty(propertyName);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }
        return defaultValue;
    }

    private String buildUrl(String host, String port, String database) {
        return "jdbc:mysql://%s:%s/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            .formatted(host, port, database);
    }

    private Properties loadFileProperties() {
        Properties properties = new Properties();
        if (!Files.exists(CONFIG_PATH)) {
            return properties;
        }
        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read database config file: " + CONFIG_PATH, exception);
        }
    }

    public record Settings(String host, String port, String databaseName, String username, String password, String url) {
    }
}
