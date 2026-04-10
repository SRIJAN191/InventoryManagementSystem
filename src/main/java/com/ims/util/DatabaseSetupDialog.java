package com.ims.util;

import com.ims.db.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class DatabaseSetupDialog {
    private DatabaseSetupDialog() {
    }

    public static Optional<DatabaseConnection.Settings> show(DatabaseConnection.Settings currentSettings,
                                                             Exception failure,
                                                             String configLocation) {
        Dialog<DatabaseConnection.Settings> dialog = new Dialog<>();
        dialog.setTitle("Database Setup");
        dialog.setHeaderText("Enter your MySQL settings to start the Inventory Management System.");

        ButtonType saveButtonType = new ButtonType("Save and Retry", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Label infoLabel = new Label("""
            The application could not connect to MySQL.
            Your settings will be saved to:
            %s
            """.formatted(configLocation));
        infoLabel.setWrapText(true);

        Label errorLabel = new Label("MySQL said: " + rootMessage(failure));
        errorLabel.setWrapText(true);

        TextField hostField = new TextField(valueOrFallback(currentSettings.host(), "localhost"));
        TextField portField = new TextField(valueOrFallback(currentSettings.port(), "3306"));
        TextField databaseField = new TextField(valueOrFallback(currentSettings.databaseName(), "ims"));
        TextField userField = new TextField(valueOrFallback(currentSettings.username(), "root"));
        PasswordField passwordField = new PasswordField();
        passwordField.setText(currentSettings.password());

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.addRow(0, new Label("Host"), hostField);
        form.addRow(1, new Label("Port"), portField);
        form.addRow(2, new Label("Database"), databaseField);
        form.addRow(3, new Label("Username"), userField);
        form.addRow(4, new Label("Password"), passwordField);

        VBox content = new VBox(12, infoLabel, errorLabel, form);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        Runnable validate = () -> saveButton.setDisable(
            hostField.getText().trim().isEmpty()
                || portField.getText().trim().isEmpty()
                || databaseField.getText().trim().isEmpty()
                || userField.getText().trim().isEmpty()
        );
        hostField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        portField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        databaseField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        userField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        validate.run();

        dialog.setResultConverter(buttonType -> {
            if (!saveButtonType.equals(buttonType)) {
                return null;
            }
            return new DatabaseConnection.Settings(
                hostField.getText().trim(),
                portField.getText().trim(),
                databaseField.getText().trim(),
                userField.getText().trim(),
                passwordField.getText(),
                ""
            );
        });
        return dialog.showAndWait();
    }

    private static String rootMessage(Exception failure) {
        Throwable rootCause = failure;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() == null ? "Unknown database error." : rootCause.getMessage();
    }

    private static String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
